/*
 * Copyright 2014-2016 the original author or authors.
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
package org.liquigraph.core.configuration;

import com.google.common.base.Charsets;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assume.assumeTrue;
import static org.junit.rules.ExpectedException.none;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ConfigurationBuilderTest {

    @Rule public ExpectedException thrown = none();
    @Rule public TemporaryFolder outputCypherFolder = new TemporaryFolder();
    @Rule public TemporaryFolder changesetFolder = new TemporaryFolder();

    @Test
    public void fails_on_null_changelog_location() {
        thrown.expect(RuntimeException.class);
        thrown.expectMessage("'masterChangelog' should not be null");

        new ConfigurationBuilder()
            .withUri("http://localhost:7474/db/data")
            .build();
    }

    @Test
    public void fails_on_non_existing_changelog() {
        thrown.expect(RuntimeException.class);
        thrown.expectMessage("'masterChangelog' points to a non-existing location: changelog/non-existing.xml");

        new ConfigurationBuilder()
            .withUri("http://localhost:7474/db/data")
            .withMasterChangelogLocation("changelog/non-existing.xml")
            .build();
    }

    @Test
    public void fails_on_null_uri() {
        thrown.expect(RuntimeException.class);
        thrown.expectMessage("- Exactly one of JDBC URI or DataSource need to be configured");

        new ConfigurationBuilder()
            .withMasterChangelogLocation("changelog/changelog.xml")
            .build();
    }

    @Test
    public void fails_on_unsupported_protocol() {
        thrown.expect(RuntimeException.class);
        thrown.expectMessage(String.format(
            "\t - Invalid JDBC URI. Supported configurations:%n" +
            "\t - jdbc:neo4j:http(s)://<host>:<port>/%n" +
            "\t - jdbc:neo4j:bolt://<host>:<port>/%n" +
            "Given: ssh://sorry@buddy"
        ));

        new ConfigurationBuilder()
            .withMasterChangelogLocation("changelog/changelog.xml")
            .withUri("ssh://sorry@buddy")
            .build();
    }

    @Test
    public void add_ups_all_misconfiguration_errors() {
        thrown.expect(RuntimeException.class);
        thrown.expectMessage(" - 'masterChangelog' should not be null");
        thrown.expectMessage(" - Exactly one of JDBC URI or DataSource need to be configured");

        new ConfigurationBuilder().build();
    }

    @Test
    public void path_should_point_to_a_directory() throws Exception {
        outputCypherFolder.create();
        Path path = outputCypherFolder.newFile("output.cypher").toPath();

        thrown.expect(RuntimeException.class);
        thrown.expectMessage(format("<%s> is not a directory", path.toString()));

        new ConfigurationBuilder()
                    .withMasterChangelogLocation("changelog/changelog.xml")
                    .withUri("file:///sorry@buddy")
                    .withDryRunMode(path)
                    .build();
    }

    @Test
    public void output_folder_must_be_writable() throws Exception {
        outputCypherFolder.create();
        Path path = outputCypherFolder.getRoot().toPath();
        assumeTrue("Folder can be made non-writable", path.toAbsolutePath().toFile().setWritable(false));

        thrown.expect(RuntimeException.class);
        thrown.expectMessage(format("<%s> must be writable", path.toString()));

        new ConfigurationBuilder()
                    .withMasterChangelogLocation("changelog/changelog.xml")
                    .withUri("http:///sorry@buddy")
                    .withDryRunMode(path)
                    .build();
    }

    @Test
    public void directly_configures_datasource() throws Exception {
        DataSource dataSource = mock(DataSource.class);
        Connection expectedConnection = mock(Connection.class);
        when(dataSource.getConnection()).thenReturn(expectedConnection);

        Configuration configuration = new ConfigurationBuilder()
            .withClassLoader(this.getClass().getClassLoader())
            .withMasterChangelogLocation("changelog/changelog.xml")
            .withDataSource(dataSource)
            .build();

        assertThat(configuration.dataSourceConfiguration().get())
            .isSameAs(expectedConnection);
    }

    @Test
    public void fails_on_password_but_no_username_provided() {
        thrown.expect(RuntimeException.class);
        thrown.expectMessage("Please provide both username and password, or none.");

        new ConfigurationBuilder()
                .withMasterChangelogLocation("changelog/changelog.xml")
                .withUri("jdbc:neo4j:http://localhost:7474")
                .withPassword("password")
                .withRunMode()
                .build();
    }

    @Test
    public void fails_on_username_but_no_password_provided() {
        thrown.expect(RuntimeException.class);
        thrown.expectMessage("Please provide both username and password, or none.");

        new ConfigurationBuilder()
                .withMasterChangelogLocation("changelog/changelog.xml")
                .withUri("jdbc:neo4j:http://localhost:7474")
                .withUsername("steve")
                .withRunMode()
                .build();
    }

    /*
     * As the name suggests, we expect that providing neither username nor
     * password is okay, because authentification can be disabled in Neo4j.
     * In this case neither is needed. See discussion on
     * https://github.com/liquigraph/liquigraph/pull/131
     */
    @Test
    public void should_not_fail_on_neither_username_nor_password_provided() {
        new ConfigurationBuilder()
                .withMasterChangelogLocation("changelog/changelog.xml")
                .withUri("jdbc:neo4j:http://localhost:7474")
                .withRunMode()
                .build();
    }

    @Test
    public void should_not_fail_with_both_username_and_password_provided() {
        new ConfigurationBuilder()
                .withMasterChangelogLocation("changelog/changelog.xml")
                .withUri("jdbc:neo4j:http://localhost:7474")
                .withUsername("steve")
                .withPassword("password")
                .withRunMode()
                .build();
    }
}
