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
package org.liquigraph.testing;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.Properties;
import java.util.function.Supplier;

public class JdbcAwareGraphDatabase {

    private final Collection<Connection> connections = new ArrayList<>();

    private final TestNeo4jContainer container;

    private final String adminPassword = "j4eon";

    private JdbcAwareGraphDatabase(boolean enterprise) {
        container = enterprise ?
            TestNeo4jContainer.createEnterpriseNeo4jContainer(adminPassword) :
            TestNeo4jContainer.createCommunityNeo4jContainer(adminPassword);
    }

    public static JdbcAwareGraphDatabase createCommunityInstance() {
        return new JdbcAwareGraphDatabase(false);
    }

    public static JdbcAwareGraphDatabase createEnterpriseInstance() {
        return new JdbcAwareGraphDatabase(true);
    }

    public JdbcAwareGraphDatabase commitNewSingleStatementConnection(String url, ThrowingConsumer<Statement, SQLException> statementConsumer) {
        return commitNewConnection(url, runSingleStatement(statementConsumer));
    }

    public JdbcAwareGraphDatabase commitNewConnection(String url, ThrowingConsumer<Connection, SQLException> connectionConsumer) {
        return doWithConnection(url, connectionConsumer.andThen(Connection::commit));
    }

    public JdbcAwareGraphDatabase rollbackNewSingleStatementConnection(String url, ThrowingConsumer<Statement, SQLException> statementConsumer) {
        return rollbackNewConnection(url, runSingleStatement(statementConsumer));
    }

    public JdbcAwareGraphDatabase rollbackNewConnection(String url, ThrowingConsumer<Connection, SQLException> connectionConsumer) {
        return doWithConnection(url, connectionConsumer.andThen(Connection::rollback));
    }

    private ThrowingConsumer<Connection, SQLException> runSingleStatement(ThrowingConsumer<Statement, SQLException> statementConsumer) {
        return connection -> {
            try (Statement statement = connection.createStatement()) {
                statementConsumer.accept(statement);
            }
        };
    }

    public void ensureStarted() {
        container.start();
    }

    public Supplier<Connection> asConnectionSupplier(String uri) {
        return () -> newConnection(uri);
    }

    public String boltJdbcUrl() {
        return String.format("jdbc:neo4j:%s", container.getBoltUrl());
    }

    public String httpJdbcUrl() {
        return String.format("jdbc:neo4j:%s", container.getHttpUrl());
    }

    public Properties props() {
        Properties props = new Properties();
        username().ifPresent(user -> props.setProperty("user", user));
        password().ifPresent(pw -> props.setProperty("password", pw));
        return props;
    }

    public Optional<String> username() {
        return Optional.of("neo4j");
    }

    public Optional<String> password() {
        return Optional.of(adminPassword);
    }

    public void cleanUp() {
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
            throw new RuntimeException(e);
        }

        if (openConnections != 0) {
            throw new RuntimeException(String.format("Connections remaining open: %d", openConnections));
        }
    }

    private void emptyDatabase() throws SQLException {
        try (Connection connection = newConnection(this.boltJdbcUrl());
             Statement statement = connection.createStatement()) {
            statement.execute("CALL clearDb()");
            connection.commit();
        }
        try (Connection connection = newConnection(this.boltJdbcUrl());
             Statement statement = connection.createStatement()) {
            statement.execute("CALL clearSchema()");
            connection.commit();
        }
    }

    private JdbcAwareGraphDatabase doWithConnection(String url, ThrowingConsumer<Connection, SQLException> connectionConsumer) {
        try (Connection connection = newConnection(url)) {
            connectionConsumer.accept(connection);
            return this;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private Connection newConnection(String uri) {
        try {
            Connection connection = DriverManager.getConnection(uri, props());
            connection.setAutoCommit(false);
            connections.add(connection);
            return connection;
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
}

