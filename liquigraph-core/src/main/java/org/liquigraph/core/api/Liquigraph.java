/*
 * Copyright 2014-2018 the original author or authors.
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

import com.google.common.annotations.VisibleForTesting;
import org.liquigraph.core.configuration.Configuration;
import org.liquigraph.core.io.ChangelogGraphReader;
import org.liquigraph.core.io.ConditionExecutor;
import org.liquigraph.core.io.ConditionPrinter;
import org.liquigraph.core.io.GraphJdbcConnector;
import org.liquigraph.core.io.LiquigraphJdbcConnector;
import org.liquigraph.core.io.xml.ChangelogParser;
import org.liquigraph.core.io.xml.ChangelogPreprocessor;
import org.liquigraph.core.io.xml.ImportResolver;
import org.liquigraph.core.io.xml.XmlSchemaValidator;
import org.liquigraph.core.validation.PersistedChangesetValidator;

/**
 * Liquigraph facade in charge of migration execution.
 */
public final class Liquigraph {

    private final MigrationRunner migrationRunner;

    public Liquigraph() {
        this(new GraphJdbcConnector());
    }

    @VisibleForTesting
    Liquigraph(LiquigraphJdbcConnector connector) {
        migrationRunner = new MigrationRunner(
                connector,
                new ChangelogParser(new XmlSchemaValidator(), new ChangelogPreprocessor(new ImportResolver())),
                new ChangelogGraphReader(),
                new ChangelogDiffMaker(),
                new ConditionExecutor(),
                new ConditionPrinter(),
                new PersistedChangesetValidator()
        );
    }

    /**
     * Triggers migration execution, according to the specified {@link org.liquigraph.core.configuration.Configuration}
     * instance.
     *
     * @param configuration configuration of the changelog location and graph connection parameters
     * @see org.liquigraph.core.configuration.ConfigurationBuilder to create {@link org.liquigraph.core.configuration.Configuration instances}
     */
    public void runMigrations(Configuration configuration) {
        migrationRunner.runMigrations(configuration);
    }
}
