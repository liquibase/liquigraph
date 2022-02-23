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

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.junit.Test;
import org.liquigraph.core.configuration.ConfigurationBuilder;
import org.liquigraph.testing.JdbcAwareGraphDatabase;
import org.liquigraph.testing.ParameterizedDatabaseIT;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class LiquigraphIT extends ParameterizedDatabaseIT {

    private final Liquigraph liquigraph = new Liquigraph();

    public LiquigraphIT(String description, JdbcAwareGraphDatabase graphDb, String uri) {
        super(description, graphDb, uri);
    }

    @Test
    public void runs_migrations_with_failed_precondition_whose_changeset_is_marked_as_executed() {
        liquigraph.runMigrations(
                new ConfigurationBuilder()
                        .withRunMode()
                        .withMasterChangelogLocation("changelog/changelog-with-1-node.xml")
                        .withUri(uri)
                        .withUsername(graphDb.username().orElse(null))
                        .withPassword(graphDb.password().orElse(null))
                        .build()
        );

        graphDb
            .rollbackNewSingleStatementConnection(uri, statement -> {
                try (ResultSet resultSet = statement.executeQuery("MATCH (human:Human {name: 'fbiville'}) RETURN human")) {
                    assertThat(resultSet.next()).isTrue();
                    assertThat(resultSet.next()).isFalse();
                }
            })
            .rollbackNewConnection(uri, connection -> {
                try (PreparedStatement statement = connection.prepareStatement("MATCH (changeset:__LiquigraphChangeset {id: ?}) RETURN changeset")) {
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
            });
    }

    @Test
    public void runs_migrations_with_schema_changes() {
        liquigraph.runMigrations(
                new ConfigurationBuilder()
                        .withRunMode()
                        .withMasterChangelogLocation("schema/schema-changelog.xml")
                        .withUri(uri)
                        .withUsername(graphDb.username().orElse(null))
                        .withPassword(graphDb.password().orElse(null))
                        .build()
        );

        graphDb.rollbackNewSingleStatementConnection(uri, statement -> {
            try (ResultSet resultSet = statement.executeQuery("MATCH (foo:Foo {bar: 123}) RETURN foo")) {
                assertThat(resultSet.next()).isTrue();
                assertThat(resultSet.next()).isFalse();
            }
        });
    }

    @Test
    public void runs_migrations_with_schema_changes_and_preconditions() {
        liquigraph.runMigrations(
                new ConfigurationBuilder()
                        .withRunMode()
                        .withMasterChangelogLocation("schema/schema-preconditions-changelog.xml")
                        .withUri(uri)
                        .withUsername(graphDb.username().orElse(null))
                        .withPassword(graphDb.password().orElse(null))
                        .build()
        );

        graphDb.rollbackNewSingleStatementConnection(uri, statement -> {
             try (ResultSet resultSet = statement.executeQuery("MATCH (foo:Foo {bar: 123}) RETURN foo")) {
                 assertThat(resultSet.next()).isTrue();
                 assertThat(resultSet.next()).isFalse();
             }
        });
    }

    @Test
    public void fails_migrations_with_edited_migration() {
        liquigraph.runMigrations(
                new ConfigurationBuilder()
                        .withRunMode()
                        .withMasterChangelogLocation("changelog/changelog.xml")
                        .withUri(uri)
                        .withUsername(graphDb.username().orElse(null))
                        .withPassword(graphDb.password().orElse(null))
                        .build()
        );

        assertThatThrownBy(() -> liquigraph.runMigrations(
                new ConfigurationBuilder()
                        .withRunMode()
                        .withMasterChangelogLocation("changelog/changelog-edited.xml")
                        .withUri(uri)
                        .withUsername(graphDb.username().orElse(null))
                        .withPassword(graphDb.password().orElse(null))
                        .build()
        ))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Changeset with ID <second-changelog> and author <team> has conflicted checksums");
    }

    @Test
    public void removes_lock_even_after_failed_migration() {
        assertThatThrownBy(() ->
            liquigraph.runMigrations(
                new ConfigurationBuilder()
                    .withRunMode()
                    .withMasterChangelogLocation("changelog/invalid_changesets/changelog-invalid-query.xml")
                    .withUri(uri)
                    .withUsername(graphDb.username().orElse(null))
                    .withPassword(graphDb.password().orElse(null))
                    .build()
            ))
        .hasCauseInstanceOf(SQLException.class)
        .hasMessageContaining("Invalid input 'n'");

        graphDb.rollbackNewSingleStatementConnection(uri, statement -> {
            try (ResultSet resultSet = statement.executeQuery("MATCH (lock:__LiquigraphLock) RETURN lock")) {
                assertThat(resultSet.next()).isFalse();
            }
        });
    }
}
