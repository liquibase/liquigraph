/*
 * Copyright 2014-2022 the original author or authors.
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
import org.liquigraph.core.io.ConditionExecutor;
import org.liquigraph.core.io.ConditionPrinter;
import org.liquigraph.core.io.GraphJdbcConnector;
import org.liquigraph.core.io.xml.ChangelogParser;
import org.liquigraph.core.io.xml.ChangelogPreprocessor;
import org.liquigraph.core.io.xml.ImportResolver;
import org.liquigraph.core.io.xml.XmlSchemaValidator;
import org.liquigraph.core.validation.PersistedChangesetValidator;

/**
 * Liquigraph facade in charge of migration execution.
 */
public class Liquigraph {

    private final MigrationRunner migrationRunner;

    public Liquigraph() {
        migrationRunner = migrationRunner(
            changelogParser(xmlSchemaValidator(), changelogPreprocessor(importResolver())),
            changelogGraphReader(),
            changelogDiffMaker(),
            conditionExecutor(),
            conditionPrinter()
        );
    }

    private static MigrationRunner migrationRunner(ChangelogParser changelogParser, ChangelogGraphReader changelogGraphReader, ChangelogDiffMaker changelogDiffMaker, ConditionExecutor conditionExecutor, ConditionPrinter conditionPrinter) {
        return new MigrationRunner(
            changelogParser,
            changelogGraphReader,
            changelogDiffMaker,
            conditionExecutor,
            conditionPrinter,
            persistedChangesetValidator()
        );
    }

    private static PersistedChangesetValidator persistedChangesetValidator() {
        return new PersistedChangesetValidator();
    }

    private static ConditionPrinter conditionPrinter() {
        return new ConditionPrinter();
    }

    private static ConditionExecutor conditionExecutor() {
        return new ConditionExecutor();
    }

    private static ChangelogDiffMaker changelogDiffMaker() {
        return new ChangelogDiffMaker();
    }

    private static ChangelogGraphReader changelogGraphReader() {
        return new ChangelogGraphReader();
    }

    private static ChangelogParser changelogParser(XmlSchemaValidator validator, ChangelogPreprocessor preprocessor) {
        return new ChangelogParser(validator, preprocessor);
    }

    private static ChangelogPreprocessor changelogPreprocessor(ImportResolver resolver) {
        return new ChangelogPreprocessor(resolver);
    }

    private static ImportResolver importResolver() {
        return new ImportResolver();
    }

    private static XmlSchemaValidator xmlSchemaValidator() {
        return new XmlSchemaValidator();
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
