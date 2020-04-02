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
package org.liquigraph.core;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

import org.junit.rules.ExternalResource;
import org.liquigraph.core.exception.Throwables;
import org.neo4j.harness.Neo4j;
import org.neo4j.harness.Neo4jBuilder;
import org.neo4j.harness.internal.InProcessNeo4jBuilder;

import static org.assertj.core.api.Assertions.assertThat;

public class BoltGraphDatabaseRule extends ExternalResource
implements GraphDatabaseRule {

    private final Neo4jBuilder builder;
    private Neo4j controls;
    private Collection<Connection> connections = new ArrayList<>();

    public BoltGraphDatabaseRule() {
        builder = new InProcessNeo4jBuilder();
    }

    @Override
    public Connection newConnection() {
        try {
            Connection connection = DriverManager.getConnection(uri());
            connection.setAutoCommit(false);
            connections.add(connection);
            return connection;
        }
        catch (SQLException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Override
    public String uri() {
        return "jdbc:neo4j:" + controls.boltURI() + "?noSsl";
    }

    @Override
    public Optional<String> username() {
        return Optional.empty();
    }

    @Override
    public Optional<String> password() {
        return Optional.empty();
    }

    protected void before() {
        controls = builder.build();
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
        }
        catch (SQLException e) {
            throw Throwables.propagate(e);
        }
        finally {
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
