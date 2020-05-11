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
package org.liquigraph.core.api;

import org.junit.Before;
import org.junit.Test;
import org.liquigraph.core.GraphIntegrationTestSuite;
import org.liquigraph.core.configuration.ConfigurationBuilder;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.slf4j.bridge.SLF4JBridgeHandler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.liquigraph.core.configuration.ClearChecksumMode.CLEAR_CHECKSUM_MODE;

abstract class LiquigraphTestSuite implements GraphIntegrationTestSuite {

    static {
        SLF4JBridgeHandler.removeHandlersForRootLogger();
    }

    private Liquigraph liquigraph;

    @Before
    public void prepare() {
        liquigraph = new Liquigraph();
    }

    @Test
    public void runs_migrations_against_embedded_graph_with_failed_precondition_whose_changeset_is_marked_as_executed() throws SQLException {
        liquigraph.runMigrations(
                new ConfigurationBuilder()
                        .withRunMode()
                        .withMasterChangelogLocation("changelog/changelog-with-1-node.xml")
                        .withUri(graphDatabase().uri())
                        .withUsername(graphDatabase().username().orElse(null))
                        .withPassword(graphDatabase().password().orElse(null))
                        .build()
        );

        try (Connection connection = graphDatabase().newConnection()) {
            try (Statement statement = connection.createStatement();
                 ResultSet resultSet = statement.executeQuery(
                         "MATCH (human:Human {name: 'fbiville'}) RETURN human"
                 )) {

                assertThat(resultSet.next()).isTrue();
                assertThat(resultSet.next()).isFalse();
            }

            try (PreparedStatement statement = connection.prepareStatement(
                    "MATCH (changeset:__LiquigraphChangeset {id: ?}) RETURN changeset"
            )) {

                statement.setObject(1, "insert-fbiville");
                try (ResultSet resultSet = statement.executeQuery()) {
                    assertThat(resultSet.next()).isTrue();
                    assertThat(resultSet.next()).isFalse();
                }

                statement.setObject(1, "insert-fbiville-again");
                try (ResultSet resultSet = statement.executeQuery()) {
                    assertThat(resultSet.next()).isTrue();
                    assertThat(resultSet.next()).isFalse();
                }
            }
            connection.rollback();
        }
    }

    @Test
    public void runs_migrations_with_schema_changes() throws SQLException {
        liquigraph.runMigrations(
                new ConfigurationBuilder()
                        .withRunMode()
                        .withMasterChangelogLocation("schema/schema-changelog.xml")
                        .withUri(graphDatabase().uri())
                        .withUsername(graphDatabase().username().orElse(null))
                        .withPassword(graphDatabase().password().orElse(null))
                        .build()
        );

        try (Connection connection = graphDatabase().newConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("MATCH (foo:Foo {bar: 123}) RETURN foo")) {

            assertThat(resultSet.next()).isTrue();
            assertThat(resultSet.next()).isFalse();
            connection.rollback();
        }
    }

    @Test
    public void runs_migrations_with_schema_changes_and_preconditions() throws SQLException {
        liquigraph.runMigrations(
                new ConfigurationBuilder()
                        .withRunMode()
                        .withMasterChangelogLocation("schema/schema-preconditions-changelog.xml")
                        .withUri(graphDatabase().uri())
                        .withUsername(graphDatabase().username().orElse(null))
                        .withPassword(graphDatabase().password().orElse(null))
                        .build()
        );

        try (Connection connection = graphDatabase().newConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("MATCH (foo:Foo {bar: 123}) RETURN foo")) {

            assertThat(resultSet.next()).isTrue();
            assertThat(resultSet.next()).isFalse();
            connection.rollback();
        }
    }

    @Test
    public void fails_migrations_with_edited_migration() {
        liquigraph.runMigrations(
                new ConfigurationBuilder()
                        .withRunMode()
                        .withMasterChangelogLocation("changelog/changelog.xml")
                        .withUri(graphDatabase().uri())
                        .withUsername(graphDatabase().username().orElse(null))
                        .withPassword(graphDatabase().password().orElse(null))
                        .build()
        );

        assertThatThrownBy(() -> liquigraph.runMigrations(
                new ConfigurationBuilder()
                        .withRunMode()
                        .withMasterChangelogLocation("changelog/changelog-edited.xml")
                        .withUri(graphDatabase().uri())
                        .withUsername(graphDatabase().username().orElse(null))
                        .withPassword(graphDatabase().password().orElse(null))
                        .build()
        ))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Changeset with ID <second-changelog> and author <team> has conflicted checksums");
    }

    @Test
    public void migrations_with_empty_checksum_should_be_updated_and_not_executed() throws SQLException {
        liquigraph.runMigrations(
            new ConfigurationBuilder()
                .withRunMode()
                .withMasterChangelogLocation("changelog/changelog-with-2-nodes.xml")
                .withUri(graphDatabase().uri())
                .build()
        );
        try (Connection connection = graphDatabase().newConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("MATCH (h:Human) RETURN count(h)")) {

            assertThat(resultSet.next()).isTrue();
            assertThat(resultSet.getInt(1)).isEqualTo(2);
            connection.rollback();
        }

        liquigraph.runMigrations(
            new ConfigurationBuilder()
                .withExecutionMode(CLEAR_CHECKSUM_MODE)
                .withMasterChangelogLocation("changelog/changelog-with-2-nodes.xml")
                .withUri(graphDatabase().uri())
                .build()
        );
        try (Connection connection = graphDatabase().newConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("MATCH (h:Human) RETURN count(h)");
             ResultSet changesetResultSet = statement.executeQuery("MATCH (changeset:__LiquigraphChangeset) WHERE NOT EXISTS(changeset.checksum) RETURN count(changeset)")) {

            assertThat(resultSet.next()).isTrue();
            assertThat(resultSet.getInt(1)).isEqualTo(2);
            assertThat(changesetResultSet.next()).isTrue();
            assertThat(changesetResultSet.getInt(1)).isEqualTo(2);
            connection.rollback();
        }

        liquigraph.runMigrations(
            new ConfigurationBuilder()
                .withRunMode()
                .withMasterChangelogLocation("changelog/changelog-with-2-nodes.xml")
                .withUri(graphDatabase().uri())
                .build()
        );
        try (Connection connection = graphDatabase().newConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("MATCH (h:Human) RETURN count(h)");
             ResultSet changesetResultSet = statement.executeQuery("MATCH (changeset:__LiquigraphChangeset) WHERE NOT EXISTS(changeset.checksum) RETURN count(changeset)")) {

            assertThat(resultSet.next()).isTrue();
            assertThat(resultSet.getInt(1)).isEqualTo(2);
            assertThat(changesetResultSet.next()).isTrue();
            assertThat(changesetResultSet.getInt(1)).isEqualTo(0);
            connection.rollback();
        }
    }

    @Test
    public void removes_lock_even_after_failed_migration() throws SQLException {
        assertThatThrownBy(() ->
            liquigraph.runMigrations(
                new ConfigurationBuilder()
                    .withRunMode()
                    .withMasterChangelogLocation("changelog/invalid_changesets/changelog-invalid-query.xml")
                    .withUri(graphDatabase().uri())
                    .withUsername(graphDatabase().username().orElse(null))
                    .withPassword(graphDatabase().password().orElse(null))
                    .build()
            ))
        .hasCauseInstanceOf(SQLException.class)
        .hasMessageContaining("Invalid input 'n'");

        try (Connection connection = graphDatabase().newConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("MATCH (lock:__LiquigraphLock) RETURN lock")) {

            assertThat(resultSet.next()).isFalse();
            connection.rollback();
        }
    }
}
