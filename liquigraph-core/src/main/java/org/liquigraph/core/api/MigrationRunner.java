/*
 * Copyright 2014-2020 the original author or authors.
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

import org.liquigraph.core.configuration.Configuration;
import org.liquigraph.core.io.ChangelogGraphReader;
import org.liquigraph.core.io.ChangelogWriter;
import org.liquigraph.core.io.ConditionExecutor;
import org.liquigraph.core.io.ConditionPrinter;
import org.liquigraph.core.io.GraphJdbcConnector;
import org.liquigraph.core.io.xml.ChangelogLoader;
import org.liquigraph.core.io.xml.ChangelogParser;
import org.liquigraph.core.model.Changeset;
import org.liquigraph.core.validation.PersistedChangesetValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.function.Supplier;

class MigrationRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(MigrationRunner.class);
    private final ChangelogParser changelogParser;
    private final ChangelogGraphReader changelogReader;
    private final ChangelogDiffMaker changelogDiffMaker;
    private final ConditionExecutor conditionExecutor;
    private final ConditionPrinter conditionPrinter;
    private final PersistedChangesetValidator persistedChangesetValidator;

    public MigrationRunner(ChangelogParser changelogParser,
                           ChangelogGraphReader changelogGraphReader,
                           ChangelogDiffMaker changelogDiffMaker,
                           ConditionExecutor conditionExecutor,
                           ConditionPrinter conditionPrinter,
                           PersistedChangesetValidator persistedChangesetValidator) {

        this.changelogParser = changelogParser;
        this.changelogReader = changelogGraphReader;
        this.changelogDiffMaker = changelogDiffMaker;
        this.conditionExecutor = conditionExecutor;
        this.conditionPrinter = conditionPrinter;
        this.persistedChangesetValidator = persistedChangesetValidator;
    }


    public void runMigrations(Configuration configuration) {
        Collection<Changeset> declaredChangesets = parseChangesets(configuration.changelogLoader(), configuration.masterChangelog());
        Supplier<Connection> connectionSupplier = new ConnectionSupplier(new GraphJdbcConnector(configuration));
        try (Connection writeConnection = connectionSupplier.get()) {
            Collection<Changeset> persistedChangesets = readPersistedChangesets(declaredChangesets, writeConnection);

            Collection<Changeset> changelog = changelogDiffMaker.computeChangesetsToInsert(
                configuration.executionContexts(),
                declaredChangesets,
                persistedChangesets
            );

            writeApplicableChangesets(configuration, writeConnection, connectionSupplier, changelog);
        } catch (SQLException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    private Collection<Changeset> parseChangesets(ChangelogLoader changelogLoader, String masterChangelog) {
        return changelogParser.parse(changelogLoader, masterChangelog);
    }

    private Collection<Changeset> readPersistedChangesets(Collection<Changeset> declaredChangesets, Connection writeConnection) {
        Collection<Changeset> persistedChangesets = changelogReader.read(writeConnection);
        Collection<String> errors = persistedChangesetValidator.validate(declaredChangesets, persistedChangesets);
        if (!errors.isEmpty()) {
            throw new IllegalArgumentException(formatErrorMessage(errors));
        }
        return persistedChangesets;
    }

    private void writeApplicableChangesets(Configuration configuration,
                                           Connection writeConnection,
                                           Supplier<Connection> connectionSupplier,
                                           Collection<Changeset> changelogsToInsert) {
        ChangelogWriter changelogWriter = configuration.resolveWriter(
          writeConnection,
          connectionSupplier,
          conditionExecutor,
          conditionPrinter
        );
        changelogWriter.write(changelogsToInsert);
    }

    private String formatErrorMessage(Collection<String> errors) {
        String separator = "\n\t";
        return separator + String.join(separator, errors);
    }

    private static class ConnectionSupplier implements Supplier<Connection> {

        private final GraphJdbcConnector connector;

        public ConnectionSupplier(GraphJdbcConnector connector) {
            this.connector = connector;
        }

        @Override
        public Connection get() {
            return connector.connect();
        }
    }
}
