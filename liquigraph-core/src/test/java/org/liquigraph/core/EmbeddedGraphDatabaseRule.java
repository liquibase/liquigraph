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
package org.liquigraph.core;

import com.google.common.base.Optional;
import org.junit.rules.ExternalResource;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.test.TestGraphDatabaseFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Properties;
import java.util.UUID;

import static com.google.common.base.Optional.absent;
import static com.google.common.base.Throwables.propagate;

public class EmbeddedGraphDatabaseRule extends ExternalResource
                                       implements GraphDatabaseRule {

    private final String dbName;
    private final String uri;
    private Collection<Connection> connections = new ArrayList<>();
    private GraphDatabaseService db;
    
    public EmbeddedGraphDatabaseRule(String name) {
        dbName = name + "-" + UUID.randomUUID().toString();
        uri = "jdbc:neo4j:instance:" + dbName;
    }

    @Override
    public Connection connection() {
        try {
            Properties props = properties();
            Connection connection = DriverManager.getConnection(uri, props);
            connection.setAutoCommit(false);
            connections.add(connection);
            return connection;
        } catch (SQLException e) {
            throw propagate(e);
        }
    }

    @Override
    public String uri() {
        return uri;
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
        try {
            db = new TestGraphDatabaseFactory().newImpermanentDatabase();
            Class.forName("org.neo4j.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw propagate(e);
        }
    }

    protected void after() {
        try {
            for (Connection connection : connections) {
                if (!connection.isClosed()) {
                    connection.close();
                }
            }
            db.shutdown();
        } catch (SQLException e) {
            throw propagate(e);
        }
    }

    private Properties properties() {
        Properties props = new Properties();
        props.put(dbName, db);
        return props;
    }
}
