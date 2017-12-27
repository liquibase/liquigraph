/*
 * Copyright 2014-2018 the original author or authors.
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
package org.liquigraph.core;

import com.google.common.base.Optional;
import com.google.common.base.Throwables;
import java.sql.ResultSet;
import java.sql.Statement;
import org.junit.rules.ExternalResource;
import org.neo4j.harness.ServerControls;
import org.neo4j.harness.TestServerBuilder;
import org.neo4j.harness.TestServerBuilders;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

import static com.google.common.base.Optional.absent;
import static org.assertj.core.api.Assertions.assertThat;

public class BoltGraphDatabaseRule extends ExternalResource
                                   implements GraphDatabaseRule {

    private final TestServerBuilder builder;
    private ServerControls controls;
    private Collection<Connection> connections = new ArrayList<>();

    public BoltGraphDatabaseRule() {
        builder = TestServerBuilders.newInProcessBuilder();
    }

    @Override
    public Connection newConnection() {
        try {
            Connection connection = DriverManager.getConnection(uri());
            connection.setAutoCommit(false);
            connections.add(connection);
            return connection;
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Override
    public String uri() {
        return "jdbc:neo4j:" + controls.boltURI() + "?noSsl";
    }

    @Override
    public Optional<String> username() {
        return absent();
    }

    @Override
    public Optional<String> password() {
        return absent();
    }

    protected void before() {
        controls = builder.newServer();
    }

    protected void after() {
        int openConnections = 0;
        try {
            emptyDatabase();
            for (Connection connection : connections) {
                if (!connection.isClosed()) {
                    openConnections++;
                    connection.close();
                }
            }
        } catch (SQLException e) {
            throw Throwables.propagate(e);
        } finally {
            controls.close();
        }
        assertThat(openConnections).as("Connections remaining open").isEqualTo(0);
    }

    private void emptyDatabase() throws SQLException {
        try (Connection connection = newConnection()) {
            try (Statement statement = connection.createStatement()) {
                statement.execute("MATCH (n) DETACH DELETE n");
            }
            connection.commit();
        }
        try (Connection connection = newConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("MATCH (n) RETURN n")) {

            assertThat(resultSet.next()).isFalse();
        }
    }

}
