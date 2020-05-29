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

import java.util.Objects;

import com.beust.jcommander.Parameters;
import com.beust.jcommander.ParametersDelegate;
import org.liquigraph.cli.commands.delegates.Connectivity;
import org.liquigraph.cli.commands.delegates.MigrationConfiguration;
import org.liquigraph.core.api.LiquigraphApi;
import org.liquigraph.core.configuration.Configuration;
import org.liquigraph.core.configuration.ConfigurationBuilder;

@Parameters(commandDescription = "Execute Liquigraph migrations")
public final class Run implements LiquigraphCommand {

    @ParametersDelegate
    private final Connectivity connectivity = new Connectivity();

    @ParametersDelegate
    private final MigrationConfiguration migrationConfiguration = new MigrationConfiguration();

    @Override
    public void accept(LiquigraphApi liquigraphApi) {
        Configuration configuration = new ConfigurationBuilder()
            .withMasterChangelogLocation(migrationConfiguration.getChangelog())
            .withExecutionContexts(migrationConfiguration.getExecutionContexts())
            .withChangelogLoader(migrationConfiguration.getChangelogLoader())
            .withUri(connectivity.getGraphDbUri())
            .withDatabase(connectivity.getDatabase())
            .withUsername(connectivity.getUsername())
            .withPassword(connectivity.getPassword())
            .withRunMode()
            .build();

        liquigraphApi.runMigrations(configuration);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Run run = (Run) o;
        return Objects.equals(connectivity, run.connectivity) &&
            Objects.equals(migrationConfiguration, run.migrationConfiguration);
    }

    @Override
    public int hashCode() {
        return Objects.hash(connectivity, migrationConfiguration);
    }
}
