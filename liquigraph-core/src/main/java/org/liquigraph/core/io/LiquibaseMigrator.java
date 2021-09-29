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
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.DatabaseException;
import liquibase.ext.neo4j.changelog.Neo4jChangelogHistoryService;
import liquibase.ext.neo4j.database.Neo4jDatabase;
import liquibase.precondition.core.AndPrecondition;
import liquibase.precondition.core.OrPrecondition;
import liquibase.precondition.core.PreconditionContainer;
import liquibase.precondition.core.PreconditionContainer.ErrorOption;
import liquibase.precondition.core.PreconditionContainer.FailOption;
import liquibase.precondition.core.SqlPrecondition;
import liquibase.serializer.ChangeLogSerializer;
import liquibase.serializer.ChangeLogSerializerFactory;
import org.liquigraph.core.configuration.ConnectionConfiguration;
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
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.joining;

public class LiquibaseMigrator {

    private final ChangelogParser parser;

    private final ChangelogGraphReader reader;

    private final ChangeLogSerializer liquibaseFileSerializer;

    private final Neo4jChangelogHistoryService graphWriter;

    private final Map<ChangeSetId, Collection<String>> contextsPerChangeSet = new HashMap<>();

    public LiquibaseMigrator(ChangelogParser parser, ChangelogGraphReader reader) {
        this(parser,
            reader,
            ChangeLogSerializerFactory.getInstance().getSerializer("xml"),
            getNeo4jHistoryService());
    }

    // visible for testing
    LiquibaseMigrator(ChangelogParser parser,
                      ChangelogGraphReader reader,
                      ChangeLogSerializer fileSerializer,
                      Neo4jChangelogHistoryService graphWriter) {
        this.parser = parser;
        this.reader = reader;
        this.liquibaseFileSerializer = fileSerializer;
        this.graphWriter = graphWriter;
    }

    public void migrateDeclaredChangeSets(String mainChangeLog, Collection<String> executionContexts, File targetFile, ChangelogLoader changelogLoader) {
        String targetFileName = targetFile.getName();
        List<Changeset> liquigraphChangeSets = parseLiquigraphChangeSets(mainChangeLog, executionContexts, changelogLoader);
        List<ChangeSet> liquibaseChangeSets = convertDeclaredChangeSets(liquigraphChangeSets, targetFileName);
        liquibaseChangeSets.forEach(changeSet -> {
            ChangeSetId changeSetId = new ChangeSetId(changeSet.getId(), changeSet.getAuthor());
            contextsPerChangeSet.put(changeSetId, changeSet.getContexts().getContexts());
        });
        writeLiquibaseChangeSets(targetFile, liquibaseChangeSets);
    }

    public void migratePersistedChangeSets(ConnectionConfiguration connectionSupplier, String changelog, boolean deleteMigratedGraph) {
        try (Connection connection = connectionSupplier.get()) {
            migrateHistory(connection, changelog);
            if (deleteMigratedGraph) {
                deleteMigratedHistory(connection);
            }
        } catch (SQLException | DatabaseException e) {
            throw new RuntimeException(e);
        }
    }

    private void migrateHistory(Connection connection, String changelog) throws DatabaseException {
        Collection<Changeset> liquigraphChangeSets = reader.read(connection);
        List<ChangeSet> liquibaseChangeSets = convertPersistedChangeSets(liquigraphChangeSets, changelog);
        graphWriter.getDatabase().setConnection(new JdbcConnection(connection));
        graphWriter.init();
        for (ChangeSet changeSet : liquibaseChangeSets) {
            graphWriter.setExecType(changeSet, ChangeSet.ExecType.EXECUTED);
            graphWriter.replaceChecksum(changeSet);
        }
    }

    private void deleteMigratedHistory(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute(
                "OPTIONAL MATCH (cl:__LiquigraphChangelog)\n" +
                    "OPTIONAL MATCH (cs:__LiquigraphChangeset)\n" +
                    "OPTIONAL MATCH (cq:__LiquigraphQuery)\n" +
                    "DETACH DELETE cl, cs, cq");
            connection.commit();
        }
    }

    private List<Changeset> parseLiquigraphChangeSets(String mainChangeLog, Collection<String> executionContexts, ChangelogLoader changelogLoader) {
        ExecutionContexts contexts = new ExecutionContexts(executionContexts);
        return validateLiquigraphChangeSets(parser.parse(changelogLoader, mainChangeLog).stream().filter(contexts::matches).collect(Collectors.toList()));
    }

    private void writeLiquibaseChangeSets(File targetFile, List<ChangeSet> liquibaseChangeSets) {
        try (FileOutputStream stream = new FileOutputStream(targetFile)) {
            this.liquibaseFileSerializer.write(liquibaseChangeSets, stream);
        } catch (IOException e) {
            throw new RuntimeException("Could not migrate to Liquibase change sets", e);
        }
    }

    private static List<ChangeSet> convertDeclaredChangeSets(Collection<Changeset> liquigraphChangeSets, String changelog) {
        return liquigraphChangeSets.stream()
            .map(liquigraphChangeSet -> migrateDeclaredChangeSets(liquigraphChangeSet, changelog))
            .collect(Collectors.toList());
    }

    private List<ChangeSet> convertPersistedChangeSets(Collection<Changeset> liquigraphChangeSets, String changelog) {
        return liquigraphChangeSets.stream()
            .map(liquigraphChangeSet -> migratePersistedChangeSets(liquigraphChangeSet, changelog))
            .collect(Collectors.toList());
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

    private static ChangeSet migrateDeclaredChangeSets(Changeset liquigraphChangeSet, String changelog) {
        String contexts = String.join(",", liquigraphChangeSet.getExecutionsContexts());
        ChangeSet result = new ChangeSet(
            liquigraphChangeSet.getId(),
            liquigraphChangeSet.getAuthor(),
            liquigraphChangeSet.isRunAlways(),
            liquigraphChangeSet.isRunOnChange(),
            changelog,
            contexts,
            null,
            null
        );
        addPrecondition(liquigraphChangeSet, result);
        addQueries(liquigraphChangeSet, result);
        return result;
    }

    private ChangeSet migratePersistedChangeSets(Changeset liquigraphChangeSet, String changelog) {
        ChangeSetId key = new ChangeSetId(liquigraphChangeSet.getId(), liquigraphChangeSet.getAuthor());
        Collection<String> contexts = contextsPerChangeSet.getOrDefault(key, Collections.emptyList());
        ChangeSet result = new ChangeSet(
            liquigraphChangeSet.getId(),
            liquigraphChangeSet.getAuthor(),
            liquigraphChangeSet.isRunAlways(),
            liquigraphChangeSet.isRunOnChange(),
            changelog,
            String.join(",", contexts),
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

    private static Neo4jChangelogHistoryService getNeo4jHistoryService() {
        Neo4jChangelogHistoryService changelogHistoryService = new Neo4jChangelogHistoryService();
        changelogHistoryService.setDatabase(new Neo4jDatabase());
        return changelogHistoryService;
    }
}

class MigrationException extends RuntimeException {
    public MigrationException(String message, Object... args) {
        super(String.format(message, args));
    }
}

class ChangeSetId {
    private final String id;
    private final String author;

    public ChangeSetId(String id, String author) {
        this.id = id;
        this.author = author;
    }

    public String getId() {
        return id;
    }

    public String getAuthor() {
        return author;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChangeSetId that = (ChangeSetId) o;
        return Objects.equals(id, that.id) && Objects.equals(author, that.author);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, author);
    }
}
