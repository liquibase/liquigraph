/*
 * Copyright 2014-2021 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.liquigraph.core.io;

import liquibase.change.core.RawSQLChange;
import liquibase.changelog.ChangeSet;
import liquibase.precondition.core.AndPrecondition;
import liquibase.precondition.core.OrPrecondition;
import liquibase.precondition.core.PreconditionContainer;
import liquibase.precondition.core.PreconditionContainer.ErrorOption;
import liquibase.precondition.core.PreconditionContainer.FailOption;
import liquibase.precondition.core.SqlPrecondition;
import liquibase.serializer.ChangeLogSerializer;
import liquibase.serializer.ChangeLogSerializerFactory;
import org.liquigraph.core.configuration.ExecutionContexts;
import org.liquigraph.core.model.AndQuery;
import org.liquigraph.core.model.Changeset;
import org.liquigraph.core.model.OrQuery;
import org.liquigraph.core.model.Precondition;
import org.liquigraph.core.model.Query;
import org.liquigraph.core.model.SimpleQuery;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.joining;

public class LiquibaseMigrator {

    private final ChangelogParser parser;

    private final ChangeLogSerializer liquibaseChangeSetSerializer;

    public LiquibaseMigrator(ChangelogParser parser) {
        this(parser, ChangeLogSerializerFactory.getInstance().getSerializer("xml"));
    }

    // visible for testing
    LiquibaseMigrator(ChangelogParser parser, ChangeLogSerializer serializer) {
        this.parser = parser;
        this.liquibaseChangeSetSerializer = serializer;
    }

    public void migrate(String mainChangeLog, Collection<String> executionContexts, String targetDirectory, ChangelogLoader changelogLoader) {
        List<Changeset> liquigraphChangeSets = parseLiquigraphChangeSets(mainChangeLog, executionContexts, changelogLoader);
        List<ChangeSet> liquibaseChangeSets = convertToLiquibaseChangeSets(liquigraphChangeSets);
        writeLiquibaseChangeSets(mainChangeLog, targetDirectory, liquibaseChangeSets);
    }

    private List<Changeset> parseLiquigraphChangeSets(String mainChangeLog, Collection<String> executionContexts, ChangelogLoader changelogLoader) {
        ExecutionContexts contexts = new ExecutionContexts(executionContexts);
        return validateLiquigraphChangeSets(parser.parse(changelogLoader, mainChangeLog).stream().filter(contexts::matches).collect(Collectors.toList()));
    }

    private void writeLiquibaseChangeSets(String mainChangeLog, String targetDirectory, List<ChangeSet> liquibaseChangeSets) {
        String fileName = new File(mainChangeLog).getName();
        File targetFile = new File(targetDirectory, String.format("%s.liquibase.xml", fileName.replaceAll("\\.xml$", "")));
        try (FileOutputStream stream = new FileOutputStream(targetFile)) {
            this.liquibaseChangeSetSerializer.write(liquibaseChangeSets, stream);
        } catch (IOException e) {
            throw new RuntimeException("Could not migrate to Liquibase change sets", e);
        }
    }

    private static List<ChangeSet> convertToLiquibaseChangeSets(List<Changeset> liquigraphChangeSets) {
        return liquigraphChangeSets.stream().map(LiquibaseMigrator::migrate).collect(Collectors.toList());
    }

    private static List<Changeset> validateLiquigraphChangeSets(List<Changeset> changeSets) {
        String withPostconditions = changeSets.stream()
            .filter(cs -> cs.getPostcondition() != null)
            .map(cs -> String.format("%s by %s", cs.getId(), cs.getAuthor()))
            .collect(joining(", "));

        if (!withPostconditions.isEmpty()) {
            throw new MigrationException(
                "\nThe following change sets define post-conditions: %s.\n" +
                    "This is not supported by Liquibase.\n" +
                    "Aborting migration now.",
                withPostconditions);
        }
        return changeSets;
    }

    private static ChangeSet migrate(Changeset liquigraphChangeSet) {
        String contexts = String.join(",", liquigraphChangeSet.getExecutionsContexts());
        ChangeSet result = new ChangeSet(
            liquigraphChangeSet.getId(),
            liquigraphChangeSet.getAuthor(),
            liquigraphChangeSet.isRunAlways(),
            liquigraphChangeSet.isRunOnChange(),
            null,
            contexts,
            null,
            null
        );
        addPrecondition(liquigraphChangeSet, result);
        addQueries(liquigraphChangeSet, result);
        return result;
    }

    private static void addPrecondition(Changeset liquigraphChangeSet, ChangeSet result) {
        Precondition liquigraphPrecondition = liquigraphChangeSet.getPrecondition();
        if (liquigraphPrecondition == null) {
            return;
        }
        result.setPreconditions(convertPrecondition(liquigraphPrecondition));
    }

    private static PreconditionContainer convertPrecondition(Precondition liquigraphPrecondition) {
        PreconditionContainer precondition = new PreconditionContainer();
        migratePreconditionPolicy(liquigraphPrecondition, precondition);
        precondition.addNestedPrecondition(traversePreconditionQuery(liquigraphPrecondition.getQuery()));
        return precondition;
    }

    private static void migratePreconditionPolicy(Precondition liquigraphPrecondition, PreconditionContainer precondition) {
        switch (liquigraphPrecondition.getPolicy()) {
            case CONTINUE:
                precondition.setOnFail(FailOption.CONTINUE);
                precondition.setOnError(ErrorOption.CONTINUE);
                break;
            case MARK_AS_EXECUTED:
                precondition.setOnFail(FailOption.MARK_RAN);
                precondition.setOnError(ErrorOption.MARK_RAN);
                break;
            case FAIL:
                precondition.setOnFail(FailOption.HALT);
                precondition.setOnError(ErrorOption.HALT);
                break;
        }
    }

    private static liquibase.precondition.Precondition traversePreconditionQuery(Query query) {
        if (query instanceof AndQuery) {
            AndQuery andQuery = (AndQuery) query;
            AndPrecondition result = new AndPrecondition();
            result.addNestedPrecondition(traversePreconditionQuery(andQuery.getFirstQuery()));
            result.addNestedPrecondition(traversePreconditionQuery(andQuery.getSecondQuery()));
            return result;
        }
        if (query instanceof OrQuery) {
            OrQuery orQuery = (OrQuery) query;
            OrPrecondition result = new OrPrecondition();
            result.addNestedPrecondition(traversePreconditionQuery(orQuery.getFirstQuery()));
            result.addNestedPrecondition(traversePreconditionQuery(orQuery.getSecondQuery()));
            return result;
        }
        if (query instanceof SimpleQuery) {
            SimpleQuery simpleQuery = (SimpleQuery) query;
            SqlPrecondition result = new SqlPrecondition();
            result.setExpectedResult("true");
            result.setSql(simpleQuery.getQuery());
            return result;
        }
        throw new IllegalArgumentException(String.format("Unsupported Liquigraph precondition query type: %s", query.getClass().getName()));
    }

    private static void addQueries(Changeset liquigraphChangeSet, ChangeSet result) {
        liquigraphChangeSet.getQueries()
            .stream()
            .map(RawSQLChange::new)
            .forEach(result::addChange);
    }
}

class MigrationException extends RuntimeException {
    public MigrationException(String message, Object... args) {
        super(String.format(message, args));
    }
}
