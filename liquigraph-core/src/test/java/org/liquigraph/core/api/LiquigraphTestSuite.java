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
package org.liquigraph.core.api;

import org.assertj.core.api.ThrowableAssert;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.liquigraph.core.GraphIntegrationTestSuite;
import org.liquigraph.core.configuration.ConfigurationBuilder;
import org.liquigraph.core.io.FixedConnectionConnector;
import org.liquigraph.core.io.GraphJdbcConnector;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.Statement;
import java.util.logging.Logger;

import javax.sql.DataSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public abstract class LiquigraphTestSuite implements GraphIntegrationTestSuite {

    private Liquigraph liquigraph;
    private Connection connection;

    @Before
    public void prepare() {
        connection = graphDatabase().connection();
        liquigraph = new Liquigraph(
                // bypasses the configured URI
                new FixedConnectionConnector(connection)
        );
    }

    @After
    public void close()
            throws SQLException {
        connection.close();
    }

    @Test
    public void runs_migrations_against_embedded_graph_with_failed_precondition_whose_changeset_is_marked_as_executed() throws SQLException {
        liquigraph.runMigrations(
                new ConfigurationBuilder()
                        .withRunMode()
                        .withMasterChangelogLocation("changelog/changelog-with-1-node.xml")
                        .withUri(graphDatabase().uri())
                        .withUsername(graphDatabase().username().orNull())
                        .withPassword(graphDatabase().password().orNull())
                        .build()
        );

        try (Connection connection = graphDatabase().connection()) {
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
                        .withUri(graphDatabase().uri())
                        .withUsername(graphDatabase().username().orNull())
                        .withPassword(graphDatabase().password().orNull())
                        .build()
        );

        try (Connection connection = graphDatabase().connection();
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
                        .withUri(graphDatabase().uri())
                        .build()
        );

        try (Connection connection = graphDatabase().connection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("MATCH (foo:Foo {bar: 123}) RETURN foo")) {

            assertThat(resultSet.next()).isTrue();
            assertThat(resultSet.next()).isFalse();
            connection.commit();
        }
    }

    @Test
    public void fails_migrations_with_edited_migration() throws SQLException {
        liquigraph.runMigrations(
                new ConfigurationBuilder()
                        .withRunMode()
                        .withMasterChangelogLocation("changelog/changelog.xml")
                        .withUri(graphDatabase().uri())
                        .build()
        );

        assertThatThrownBy(new ThrowableAssert.ThrowingCallable() {
            @Override
            public void call() throws Throwable {
                liquigraph.runMigrations(
                        new ConfigurationBuilder()
                                .withRunMode()
                                .withMasterChangelogLocation("changelog/changelog-edited.xml")
                                .withUri(graphDatabase().uri())
                                .build()
                );
            }
        })
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Changeset with ID <second-changelog> and author <team> has conflicted checksums");
    }

    @Test
    public void does_not_leave_lock_when_invalid_statement_is_executed() throws Exception {
        assertThatThrownBy(new ThrowableAssert.ThrowingCallable() {
            @Override
            public void call() throws Throwable {
                Liquigraph liquigraph1 = new Liquigraph(
                		new GraphJdbcConnector() // for reproducing problem needed LocableConnection
                );
                
                liquigraph1.runMigrations(
                        new ConfigurationBuilder()
                                .withRunMode()
                                .withMasterChangelogLocation("changelog/changelog-invalid-statement.xml")
                                .withDataSource(new DataSource(){
									@Override
									public PrintWriter getLogWriter() throws SQLException {
										return null;
									}

									@Override
									public void setLogWriter(PrintWriter out) throws SQLException {
									}

									@Override
									public void setLoginTimeout(int seconds) throws SQLException {
									}

									@Override
									public int getLoginTimeout() throws SQLException {
										return 0;
									}

									@Override
									public Logger getParentLogger() throws SQLFeatureNotSupportedException {
										return null;
									}

									@Override
									public <T> T unwrap(Class<T> iface) throws SQLException {
										return null;
									}

									@Override
									public boolean isWrapperFor(Class<?> iface) throws SQLException {
										return false;
									}

									@Override
									public Connection getConnection() throws SQLException {
										return graphDatabase().connection();
									}

									@Override
									public Connection getConnection(String username, String password)
											throws SQLException {
										return graphDatabase().connection();
									}
                                	
                                })
                                .build()
                );
            }
        })
                .isInstanceOf(RuntimeException.class)
                .hasCauseInstanceOf(SQLException.class);

        try (Connection connection = graphDatabase().connection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("MATCH (n:__LiquigraphLock) RETURN n")) {

            assertThat(resultSet.next()).isFalse();
        }
    }
}
