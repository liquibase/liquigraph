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
import org.liquigraph.cli.commands.delegates.MigrationConfiguration;
import org.liquigraph.cli.io.ClassLoaders;
import org.liquigraph.cli.io.Files;
import org.liquigraph.core.api.LiquigraphApi;
import org.liquigraph.core.io.ChangelogLoader;
import org.liquigraph.core.io.ClassLoaderChangelogLoader;

import java.util.Objects;

@Parameters(commandDescription = "Migrates the given declared change sets to Liquibase XML format")
public final class MigrateDeclaredChangeSets implements LiquigraphCommand {

    @ParametersDelegate
    private final MigrationConfiguration migrationConfiguration = new MigrationConfiguration();

    @Parameter(
        names = {"--target-directory", "-d"},
        description = "Output directory path into which the Liquibase change sets will be written. " +
            "The output file will be named <basename>.liquibase.xml",
        required = true
    )
    private String targetDirectory;

    @Override
    public void accept(LiquigraphApi liquigraphApi) {
        liquigraphApi.migrateDeclaredChangeSets(
            migrationConfiguration.getChangelog(),
            migrationConfiguration.getExecutionContexts(),
            targetDirectory,
            getChangelogLoader()
        );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MigrateDeclaredChangeSets migrateLiquibase = (MigrateDeclaredChangeSets) o;
        return Objects.equals(migrationConfiguration, migrateLiquibase.migrationConfiguration) &&
            Objects.equals(targetDirectory, migrateLiquibase.targetDirectory);
    }

    @Override
    public int hashCode() {
        return Objects.hash(migrationConfiguration, targetDirectory);
    }

    private ChangelogLoader getChangelogLoader() {
        return new ClassLoaderChangelogLoader(
            ClassLoaders.urlClassLoader(
                migrationConfiguration.getResourceUrl(),
                Files.toUrl(targetDirectory)
            )
        );
    }
}
