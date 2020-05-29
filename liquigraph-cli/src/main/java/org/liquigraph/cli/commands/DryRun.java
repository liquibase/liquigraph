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
package org.liquigraph.cli.commands;

import java.nio.file.Paths;
import java.util.Objects;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.beust.jcommander.ParametersDelegate;
import org.liquigraph.cli.commands.delegates.Connectivity;
import org.liquigraph.cli.commands.delegates.MigrationConfiguration;
import org.liquigraph.cli.io.ClassLoaders;
import org.liquigraph.cli.io.Files;
import org.liquigraph.core.api.LiquigraphApi;
import org.liquigraph.core.configuration.Configuration;
import org.liquigraph.core.configuration.ConfigurationBuilder;
import org.liquigraph.core.io.xml.ChangelogLoader;
import org.liquigraph.core.io.xml.ClassLoaderChangelogLoader;

@Parameters(commandDescription = "Simulate the execution of Liquigraph migrations")
public final class DryRun implements LiquigraphCommand {

    @ParametersDelegate
    private final Connectivity connectivity = new Connectivity();

    @ParametersDelegate
    private final MigrationConfiguration migrationConfiguration = new MigrationConfiguration();

    @Parameter(
        names = {"--dry-run-output-directory", "-d", "--output-directory"},
        description = "Output directory path into which changeset queries will be written.",
        required = true
    )
    private String dryRunOutputDirectory;

    @Override
    public void accept(LiquigraphApi liquigraphApi) {
        Configuration configuration = new ConfigurationBuilder()
            .withMasterChangelogLocation(migrationConfiguration.getChangelog())
            .withExecutionContexts(migrationConfiguration.getExecutionContexts())
            .withChangelogLoader(getChangelogLoader())
            .withUri(connectivity.getGraphDbUri())
            .withDatabase(connectivity.getDatabase())
            .withUsername(connectivity.getUsername())
            .withPassword(connectivity.getPassword())
            .withDryRunMode(Paths.get(dryRunOutputDirectory))
            .build();

        liquigraphApi.runMigrations(configuration);
    }

    private ChangelogLoader getChangelogLoader() {
        return new ClassLoaderChangelogLoader(
            ClassLoaders.urlClassLoader(
                migrationConfiguration.getResourceUrl(),
                Files.toUrl(dryRunOutputDirectory)
            )
        );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DryRun dryRun = (DryRun) o;
        return Objects.equals(connectivity, dryRun.connectivity) &&
            Objects.equals(migrationConfiguration, dryRun.migrationConfiguration) &&
            Objects.equals(dryRunOutputDirectory, dryRun.dryRunOutputDirectory);
    }

    @Override
    public int hashCode() {
        return Objects.hash(connectivity, migrationConfiguration, dryRunOutputDirectory);
    }
}
