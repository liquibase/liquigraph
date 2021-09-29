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
package org.liquigraph.cli.commands;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.beust.jcommander.ParametersDelegate;
import org.liquigraph.cli.commands.delegates.Connectivity;
import org.liquigraph.cli.commands.delegates.MigrationConfiguration;
import org.liquigraph.cli.io.ClassLoaders;
import org.liquigraph.cli.io.Files;
import org.liquigraph.core.api.LiquigraphApi;
import org.liquigraph.core.configuration.Connections;
import org.liquigraph.core.io.ChangelogLoader;
import org.liquigraph.core.io.ClassLoaderChangelogLoader;

import java.io.File;
import java.util.Objects;
import java.util.Optional;

@Parameters(commandDescription = "Migrates the change sets to Liquibase")
public final class MigrateToLiquibase implements LiquigraphCommand {

    @ParametersDelegate
    private final MigrationConfiguration migrationConfiguration = new MigrationConfiguration();

    @ParametersDelegate
    private final Connectivity connectivity = new Connectivity();

    @Parameter(
        names = {"--delete-migrated-graph", "--delete"},
        description = "Whether the Liquigraph execution history graph should be deleted after the migration completes"
    )
    private boolean deleteAfterMigration;

    @Parameter(
        names = {"--target-file", "--file", "-f"},
        description = "Output directory path into which the Liquibase change sets will be written. " +
            "The output file will be named <basename>.liquibase.xml",
        required = true
    )
    private File targetFile;

    @Override
    public void accept(LiquigraphApi liquigraphApi) {
        liquigraphApi.migrateDeclaredChangeSets(
            migrationConfiguration.getChangelog(),
            migrationConfiguration.getExecutionContexts(),
            targetFile,
            getChangelogLoader()
        );
        liquigraphApi.migratePersistedChangeSets(
            Connections.provide(
                Optional.ofNullable(connectivity.getGraphDbUri()),
                Optional.ofNullable(connectivity.getDatabase()),
                Optional.ofNullable(connectivity.getUsername()),
                Optional.ofNullable(connectivity.getPassword()),
                Optional.empty()
            ),
            targetFile.getName(),
            deleteAfterMigration
        );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MigrateToLiquibase that = (MigrateToLiquibase) o;
        return deleteAfterMigration == that.deleteAfterMigration && Objects.equals(migrationConfiguration, that.migrationConfiguration) && Objects.equals(connectivity, that.connectivity) && Objects.equals(targetFile, that.targetFile);
    }

    @Override
    public int hashCode() {
        return Objects.hash(migrationConfiguration, connectivity, deleteAfterMigration, targetFile);
    }

    private ChangelogLoader getChangelogLoader() {
        return new ClassLoaderChangelogLoader(
            ClassLoaders.urlClassLoader(
                migrationConfiguration.getResourceUrl(),
                Files.toUrl(targetFile.getParentFile())
            )
        );
    }
}
