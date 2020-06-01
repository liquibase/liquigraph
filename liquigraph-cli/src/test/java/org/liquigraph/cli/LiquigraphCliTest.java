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
package org.liquigraph.cli;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.liquigraph.core.api.LiquigraphApi;
import org.liquigraph.core.configuration.Configuration;
import org.liquigraph.core.configuration.DryRunMode;
import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.liquigraph.core.configuration.RunMode.RUN_MODE;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class LiquigraphCliTest {

    private static final PrintStream DEFAULT_STDOUT = System.out;

    private final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

    private final PrintStream customStdout = new PrintStream(outputStream);

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    private final LiquigraphApi liquigraph = mock(LiquigraphApi.class);

    private final LiquigraphCli cli = new LiquigraphCli(liquigraph);

    @Before
    public void prepare() throws Exception {
        System.setOut(customStdout);
    }

    @After
    public void clean_up() {
        System.setOut(DEFAULT_STDOUT);
        customStdout.close();
    }

    @Test
    public void shows_version() throws Exception {
        cli.execute(new String[] {"-v"});

        assertThat(commandOutput())
            .as("CLI should return valid version")
            .containsPattern(Pattern.compile("[1-9]\\d*\\.\\d+\\.\\d+(?:-SNAPSHOT)?"));
    }

    @Test
    public void shows_version_with_short_option() throws Exception {
        cli.execute(new String[] {"--version"});

        assertThat(commandOutput())
            .as("CLI should return valid version")
            .containsPattern(Pattern.compile("[1-9]\\d*\\.\\d+\\.\\d+(?:-SNAPSHOT)?"));
    }

    @Test
    public void shows_help() throws Exception {
        cli.execute(new String[] {"--help"});

        assertThat(commandOutput())
            .as("CLI should show usage")
            .startsWith("Usage: liquigraph");
    }

    @Test
    public void shows_help_with_short_option() throws Exception {
        cli.execute(new String[] {"-h"});

        assertThat(commandOutput())
            .as("CLI should show usage")
            .startsWith("Usage: liquigraph");
    }

    @Test
    public void executes_minimal_migration() {
        String uri = "jdbc:neo4j:bolt://example.com";
        String masterChangelog = "changelog.xml";

        cli.execute(new String[] {
            "--graph-db-uri", uri,
            "--changelog", masterChangelog
        });

        ArgumentCaptor<Configuration> captor = ArgumentCaptor.forClass(Configuration.class);
        verify(liquigraph).runMigrations(captor.capture());
        Configuration configuration = captor.getValue();
        assertThat(configuration.masterChangelog()).isEqualTo(masterChangelog);
        assertThat(configuration.executionMode()).isEqualTo(RUN_MODE);
    }

    @Test
    public void executes_minimal_migration_with_short_options() {
        String uri = "jdbc:neo4j:bolt://example.com";
        String masterChangelog = "changelog.xml";

        cli.execute(new String[] {
            "-g", uri,
            "-c", masterChangelog
        });

        ArgumentCaptor<Configuration> captor = ArgumentCaptor.forClass(Configuration.class);
        verify(liquigraph).runMigrations(captor.capture());
        Configuration configuration = captor.getValue();
        assertThat(configuration.masterChangelog()).isEqualTo(masterChangelog);
        assertThat(configuration.executionMode()).isEqualTo(RUN_MODE);
    }

    @Test
    public void dry_runs_minimal_migration() {
        String uri = "jdbc:neo4j:bolt://example.com";
        String masterChangelog = "changelog.xml";
        File dryRunDirectory = temporaryFolder.getRoot();

        cli.execute(new String[] {
            "--dry-run-output-directory", dryRunDirectory.getPath(),
            "--graph-db-uri", uri,
            "--changelog", masterChangelog
        });

        ArgumentCaptor<Configuration> captor = ArgumentCaptor.forClass(Configuration.class);
        verify(liquigraph).runMigrations(captor.capture());
        Configuration configuration = captor.getValue();
        assertThat(configuration.masterChangelog()).isEqualTo(masterChangelog);
        assertThat(configuration.executionMode()).isEqualTo(new DryRunMode(dryRunDirectory.toPath()));
    }

    @Test
    public void dry_runs_minimal_migration_with_short_options() {
        String uri = "jdbc:neo4j:bolt://example.com";
        String masterChangelog = "changelog.xml";
        File dryRunDirectory = temporaryFolder.getRoot();

        cli.execute(new String[] {
            "-d", dryRunDirectory.getPath(),
            "-g", uri,
            "-c", masterChangelog
        });

        ArgumentCaptor<Configuration> captor = ArgumentCaptor.forClass(Configuration.class);
        verify(liquigraph).runMigrations(captor.capture());
        Configuration configuration = captor.getValue();
        assertThat(configuration.masterChangelog()).isEqualTo(masterChangelog);
        assertThat(configuration.executionMode()).isEqualTo(new DryRunMode(dryRunDirectory.toPath()));
    }

    private String commandOutput() throws Exception {
        return outputStream.toString(StandardCharsets.UTF_8.name());
    }

}
