/**
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
package org.liquigraph.core.api;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.liquigraph.core.EmbeddedGraphDatabaseRule;
import org.liquigraph.core.configuration.ConfigurationBuilder;
import org.liquigraph.core.io.FixedConnectionConnector;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.assertj.core.api.Assertions.assertThat;

public class LiquigraphTest {

    @Rule public EmbeddedGraphDatabaseRule graph = new EmbeddedGraphDatabaseRule("neo");
    private Liquigraph liquigraph;

    @Before
    public void prepare() {
        liquigraph = new Liquigraph(
            // bypasses the configured URI
             new FixedConnectionConnector(graph.jdbcConnection())
        );
    }

    @Test
    public void runs_migrations_against_embedded_graph_with_failed_precondition_whose_changeset_is_marked_as_executed() throws SQLException {
        liquigraph.runMigrations(
                new ConfigurationBuilder()
                        .withRunMode()
                        .withMasterChangelogLocation("changelog/changelog-with-1-node.xml")
                        .withUri(graph.uri())
                        .build()
        );

        try (Connection connection = graph.jdbcConnection()) {
            try (Statement statement = connection.createStatement();
                 ResultSet resultSet = statement.executeQuery(
                     "MATCH (human:Human {name: 'fbiville'}) RETURN human"
                 )) {

                assertThat(resultSet.next()).isTrue();
                assertThat(resultSet.next()).isFalse();
                connection.commit();
            }

            try (PreparedStatement statement = connection.prepareStatement(
                "MATCH (changeset:__LiquigraphChangeset {id: {1}}) RETURN changeset"
            )) {

                statement.setObject(1, "insert-fbiville");
                ResultSet resultSet = statement.executeQuery();
                assertThat(resultSet.next()).isTrue();
                assertThat(resultSet.next()).isFalse();

                statement.setObject(1, "insert-fbiville-again");
                resultSet = statement.executeQuery();
                assertThat(resultSet.next()).isTrue();
                assertThat(resultSet.next()).isFalse();
                connection.commit();
            }
        }
    }

    @Test
    public void runs_migrations_with_schema_changes() throws SQLException {
        liquigraph.runMigrations(
            new ConfigurationBuilder()
                .withRunMode()
                .withMasterChangelogLocation("schema/schema-changelog.xml")
                .withUri(graph.uri())
                .build()
        );

        try (Connection connection = graph.jdbcConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("MATCH (foo:Foo {bar: 123}) RETURN foo")) {

            assertThat(resultSet.next()).isTrue();
            assertThat(resultSet.next()).isFalse();
            connection.commit();
        }
    }

    @Test
    public void runs_migrations_with_schema_changes_and_preconditions() throws SQLException {
        liquigraph.runMigrations(
            new ConfigurationBuilder()
                .withRunMode()
                .withMasterChangelogLocation("schema/schema-preconditions-changelog.xml")
                .withUri(graph.uri())
                .build()
        );

        try (Connection connection = graph.jdbcConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("MATCH (foo:Foo {bar: 123}) RETURN foo")) {

            assertThat(resultSet.next()).isTrue();
            assertThat(resultSet.next()).isFalse();
            connection.commit();
        }
    }

}