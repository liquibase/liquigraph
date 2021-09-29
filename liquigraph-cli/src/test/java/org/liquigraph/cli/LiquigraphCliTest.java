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
package org.liquigraph.cli;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.liquigraph.core.api.LiquigraphApi;
import org.liquigraph.core.configuration.Configuration;
import org.liquigraph.core.configuration.ConnectionConfiguration;
import org.liquigraph.core.configuration.ConnectionConfigurationByUri;
import org.liquigraph.core.configuration.DryRunMode;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Optional;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.liquigraph.core.configuration.RunMode.RUN_MODE;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
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
    public void prepare() {
        System.setOut(customStdout);
    }

    @After
    public void clean_up() {
        System.setOut(DEFAULT_STDOUT);
        customStdout.close();
    }

    @Test
    public void shows_version() throws Exception {
        cli.execute(new String[]{"-v"});

        assertThat(commandOutput())
            .as("CLI should return valid version")
            .containsPattern(Pattern.compile("[1-9]\\d*\\.\\d+\\.\\d+(?:-SNAPSHOT)?"));
    }

    @Test
    public void shows_version_with_short_option() throws Exception {
        cli.execute(new String[]{"--version"});

        assertThat(commandOutput())
            .as("CLI should return valid version")
            .containsPattern(Pattern.compile("[1-9]\\d*\\.\\d+\\.\\d+(?:-SNAPSHOT)?"));
    }

    @Test
    public void shows_help() throws Exception {
        cli.execute(new String[]{"--help"});

        assertThat(commandOutput())
            .as("CLI should show usage")
            .startsWith("Usage: liquigraph");
    }

    @Test
    public void shows_help_with_short_option() throws Exception {
        cli.execute(new String[]{"-h"});

        assertThat(commandOutput())
            .as("CLI should show usage")
            .startsWith("Usage: liquigraph");
    }

    @Test
    public void fails_with_both_version_and_help_flag() {
        assertThatThrownBy(() -> cli.execute(new String[]{"--help", "--version"}))
            .hasMessage("Either --version or --help must be set, not both")
            .isInstanceOf(RuntimeException.class);
    }

    @Test
    public void executes_minimal_migration() {
        String uri = "jdbc:neo4j:bolt://example.com";
        String mainChangelog = "changelog.xml";

        cli.execute(new String[]{
            "run",
            "--graph-db-uri", uri,
            "--changelog", mainChangelog
        });

        ArgumentCaptor<Configuration> captor = ArgumentCaptor.forClass(Configuration.class);
        verify(liquigraph).runMigrations(captor.capture());
        Configuration configuration = captor.getValue();
        assertThat(configuration.masterChangelog()).isEqualTo(mainChangelog);
        assertThat(configuration.executionMode()).isEqualTo(RUN_MODE);
    }

    @Test
    public void executes_minimal_migration_with_short_options() {
        String uri = "jdbc:neo4j:bolt://example.com";
        String mainChangelog = "changelog.xml";

        cli.execute(new String[]{
            "run",
            "-g", uri,
            "-c", mainChangelog
        });

        ArgumentCaptor<Configuration> captor = ArgumentCaptor.forClass(Configuration.class);
        verify(liquigraph).runMigrations(captor.capture());
        Configuration configuration = captor.getValue();
        assertThat(configuration.masterChangelog()).isEqualTo(mainChangelog);
        assertThat(configuration.executionMode()).isEqualTo(RUN_MODE);
    }

    @Test
    public void dry_runs_minimal_migration() {
        String uri = "jdbc:neo4j:bolt://example.com";
        String mainChangelog = "changelog.xml";
        File dryRunDirectory = temporaryFolder.getRoot();

        cli.execute(new String[]{
            "dry-run",
            "--dry-run-output-directory", dryRunDirectory.getPath(),
            "--graph-db-uri", uri,
            "--changelog", mainChangelog
        });

        ArgumentCaptor<Configuration> captor = ArgumentCaptor.forClass(Configuration.class);
        verify(liquigraph).runMigrations(captor.capture());
        Configuration configuration = captor.getValue();
        assertThat(configuration.masterChangelog()).isEqualTo(mainChangelog);
        assertThat(configuration.executionMode()).isEqualTo(new DryRunMode(dryRunDirectory.toPath()));
    }

    @Test
    public void dry_runs_minimal_migration_with_short_options() {
        String uri = "jdbc:neo4j:bolt://example.com";
        String mainChangelog = "changelog.xml";
        File dryRunDirectory = temporaryFolder.getRoot();

        cli.execute(new String[]{
            "dry-run",
            "-d", dryRunDirectory.getPath(),
            "-g", uri,
            "-c", mainChangelog
        });

        ArgumentCaptor<Configuration> captor = ArgumentCaptor.forClass(Configuration.class);
        verify(liquigraph).runMigrations(captor.capture());
        Configuration configuration = captor.getValue();
        assertThat(configuration.masterChangelog()).isEqualTo(mainChangelog);
        assertThat(configuration.executionMode()).isEqualTo(new DryRunMode(dryRunDirectory.toPath()));
    }

    @Test
    public void migrates_to_Liquibase() throws Exception {
        String uri = "jdbc:neo4j:bolt://example.com";
        String mainChangelog = "changelog.xml";
        File targetFile = temporaryFolder.newFile("result.xml");
        String username = "neo4j";
        String password = "s3cr3t";

        cli.execute(new String[]{
            "migrate-to-liquibase",
            "-f", targetFile.getPath(),
            "-x", "foo,bar",
            "-c", mainChangelog,
            "-g", uri,
            "-u", username,
            "-p", password
        });

        InOrder inOrder = inOrder(liquigraph);
        inOrder.verify(liquigraph).migrateDeclaredChangeSets(
            eq(mainChangelog),
            eq(Arrays.asList("foo", "bar")),
            eq(targetFile),
            any()
        );
        inOrder.verify(liquigraph).migratePersistedChangeSets(
            eq(new ConnectionConfigurationByUri(uri, Optional.empty(), Optional.of(username), Optional.of(password))),
            eq(targetFile.getName()),
            eq(false)
        );
    }

    @Test
    public void deletes_Liquigraph_graph_after_Liquibase_migration() throws Exception {
        String uri = "jdbc:neo4j:bolt://example.com";
        String mainChangelog = "changelog.xml";
        File targetFile = temporaryFolder.newFile("result.xml");
        String username = "neo4j";
        String password = "s3cr3t";

        cli.execute(new String[]{
            "migrate-to-liquibase",
            "-f", targetFile.getPath(),
            "-x", "foo,bar",
            "-c", mainChangelog,
            "-g", uri,
            "-u", username,
            "-p", password,
            "--delete"
        });

        verify(liquigraph).migratePersistedChangeSets(
            any(ConnectionConfiguration.class),
            anyString(),
            eq(true)
        );
    }

    private String commandOutput() throws Exception {
        return outputStream.toString(StandardCharsets.UTF_8.name());
    }

}
