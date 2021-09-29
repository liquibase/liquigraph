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
package org.liquigraph.core.api;

import org.liquigraph.core.configuration.Configuration;
import org.liquigraph.core.configuration.ConnectionConfiguration;
import org.liquigraph.core.io.ChangelogLoader;

import java.io.File;
import java.util.Collection;

public interface LiquigraphApi {

    /**
     * Triggers migration execution, according to the specified {@link org.liquigraph.core.configuration.Configuration}
     * instance.
     *
     * @param configuration configuration of the changelog location and graph connection parameters
     * @see org.liquigraph.core.configuration.ConfigurationBuilder to create {@link org.liquigraph.core.configuration.Configuration instances}
     */
    void runMigrations(Configuration configuration);

    /**
     * Migrates the provided declared change sets to the Liquibase XML format, optionally filtered by the provided execution contexts
     *
     * @param changelog changelog file location
     * @param executionContexts optional execution contexts, used to filter change sets
     * @param targetFile target file where the migrated change sets are written to
     * @param changelogLoader changelog loader
     */
    void migrateDeclaredChangeSets(String changelog, Collection<String> executionContexts, File targetFile, ChangelogLoader changelogLoader);

    /**
     * Migrates the stored change log execution history from Liquigraph to Liquibase format.
     *
     * @param connectionConfiguration configuration specifying how to connect to the target server
     * @param changelog changelog file location
     * @param deleteMigratedGraph whether the existing execution history graph should be deleted after the migration completes
     */
    void migratePersistedChangeSets(ConnectionConfiguration connectionConfiguration, String changelog, boolean deleteMigratedGraph);
}
