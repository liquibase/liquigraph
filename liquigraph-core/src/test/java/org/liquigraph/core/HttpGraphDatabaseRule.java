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
import java.sql.ResultSet;
import org.junit.Assume;
import org.junit.rules.ExternalResource;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.neo4j.jdbc.http.HttpDriver;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

import static com.google.common.base.Throwables.propagate;
import static org.assertj.core.api.Assertions.assertThat;

public class HttpGraphDatabaseRule extends ExternalResource
                                   implements GraphDatabaseRule {

    private final String uri;
    private final String username;
    private final String password;
    private final Collection<Connection> connections = new ArrayList<>();

    public HttpGraphDatabaseRule() {
        uri = "jdbc:neo4j:http://localhost:7474";
        username = "neo4j";
        password = "j4oen";
    }

    public static void assumeRemoteGraphDatabaseIsProvisioned() {
        Assume.assumeTrue(
            "Neo4j remote instance is provisioned with Docker",
            "true".equals(System.getenv("WITH_DOCKER"))
        );
    }

    @Override
    public Statement apply(Statement base, Description description) {
        assumeRemoteGraphDatabaseIsProvisioned();
        return super.apply(base, description);
    }

    @Override
    public Connection newConnection() {
        try {
            Connection connection = DriverManager.getConnection(uri, username, password);
            connection.setAutoCommit(false);
            connections.add(connection);
            return connection;
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Override
    public String uri() {
        return uri;
    }

    @Override
    public Optional<String> username() {
        return Optional.of(username);
    }

    @Override
    public Optional<String> password() {
        return Optional.of(password);
    }

    protected void before() {
        try {
            DriverManager.registerDriver(new HttpDriver());
        } catch (SQLException e) {
            throw propagate(e);
        }
    }

    @Override
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
            throw propagate(e);
        }
        assertThat(openConnections).as("Connections remaining open").isEqualTo(0);
    }

    private void emptyDatabase() throws SQLException {
        try (Connection connection = newConnection()) {
            try (java.sql.Statement statement = connection.createStatement()) {
                statement.execute("MATCH (n) DETACH DELETE n");
            }
            connection.commit();
        }
        try (Connection connection = newConnection();
             java.sql.Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("MATCH (n) RETURN n")) {

            assertThat(resultSet.next())
                .as("Database is empty")
                .isFalse();
        }
    }

}
