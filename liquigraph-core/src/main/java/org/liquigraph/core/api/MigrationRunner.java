/**
 * Copyright 2014-2016 the original author or authors.
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
package org.liquigraph.core.api;

import com.google.common.base.Joiner;
import org.liquigraph.core.configuration.Configuration;
import org.liquigraph.core.io.ChangelogGraphReader;
import org.liquigraph.core.io.ChangelogWriter;
import org.liquigraph.core.io.LiquigraphJdbcConnector;
import org.liquigraph.core.io.PreconditionExecutor;
import org.liquigraph.core.io.PreconditionPrinter;
import org.liquigraph.core.io.xml.ChangelogParser;
import org.liquigraph.core.model.Changeset;
import org.liquigraph.core.validation.PersistedChangesetValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;

class MigrationRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(MigrationRunner.class);
    private final LiquigraphJdbcConnector connector;
    private final ChangelogParser changelogParser;
    private final ChangelogGraphReader changelogReader;
    private final ChangelogDiffMaker changelogDiffMaker;
    private final PreconditionExecutor preconditionExecutor;
    private final PreconditionPrinter preconditionPrinter;
    private final PersistedChangesetValidator persistedChangesetValidator;

    public MigrationRunner(LiquigraphJdbcConnector connector,
                           ChangelogParser changelogParser,
                           ChangelogGraphReader changelogGraphReader,
                           ChangelogDiffMaker changelogDiffMaker,
                           PreconditionExecutor preconditionExecutor,
                           PreconditionPrinter preconditionPrinter,
                           PersistedChangesetValidator persistedChangesetValidator) {

        this.connector = connector;
        this.changelogParser = changelogParser;
        this.changelogReader = changelogGraphReader;
        this.changelogDiffMaker = changelogDiffMaker;
        this.preconditionExecutor = preconditionExecutor;
        this.preconditionPrinter = preconditionPrinter;
        this.persistedChangesetValidator = persistedChangesetValidator;
    }



    public void runMigrations(Configuration configuration) {
        Collection<Changeset> declaredChangesets = parseChangesets(configuration.classLoader(), configuration.masterChangelog());

        try (Connection connection = connector.connect(configuration)) {
            Collection<Changeset> persistedChangesets = readPersistedChangesets(declaredChangesets, connection);

            Collection<Changeset> changelog = changelogDiffMaker.computeChangesetsToInsert(
                configuration.executionContexts(),
                declaredChangesets,
                persistedChangesets
            );

            writeApplicableChangesets(configuration, connection, changelog);
        } catch (SQLException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    private Collection<Changeset> parseChangesets(ClassLoader classLoader, String masterChangelog) {
        return changelogParser.parse(classLoader, masterChangelog);
    }

    private Collection<Changeset> readPersistedChangesets(Collection<Changeset> declaredChangesets, Connection graphDatabase) {
        Collection<Changeset> persistedChangesets = changelogReader.read(graphDatabase);
        Collection<String> errors = persistedChangesetValidator.validate(declaredChangesets, persistedChangesets);
        if (!errors.isEmpty()) {
            throw new IllegalArgumentException(formatErrorMessage(errors));
        }
        return persistedChangesets;
    }

    private void writeApplicableChangesets(Configuration configuration, Connection connection, Collection<Changeset> changelogsToInsert) {
        ChangelogWriter changelogWriter = configuration.resolveWriter(
            connection,
            preconditionExecutor,
            preconditionPrinter
        );
        changelogWriter.write(changelogsToInsert);
    }

    private String formatErrorMessage(Collection<String> errors) {
        String separator = "\n\t";
        return separator + Joiner.on(separator).join(errors);
    }
}
