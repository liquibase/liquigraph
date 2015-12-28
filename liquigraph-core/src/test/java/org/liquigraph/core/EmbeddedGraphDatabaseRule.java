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

import org.junit.rules.ExternalResource;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.test.TestGraphDatabaseFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.UUID;

import static com.google.common.base.Throwables.propagate;

public class EmbeddedGraphDatabaseRule extends ExternalResource {

    private final String dbName;
    private final String uri;
    private Connection connection;
    private GraphDatabaseService db;
    
    public EmbeddedGraphDatabaseRule(String name) {
        dbName = name + "-" + UUID.randomUUID().toString();
        uri = "jdbc:neo4j:instance:" + dbName;
    }
    
    public Connection jdbcConnection() {
        return connection;
    }

    public String uri() {
        return uri;
    }

    protected void before() {
        try {
            db = new TestGraphDatabaseFactory().newImpermanentDatabase();
            Class.forName("org.neo4j.jdbc.Driver");
            Properties props = properties();
            connection = DriverManager.getConnection(uri, props);
            connection.setAutoCommit(false);
        } catch (ClassNotFoundException | SQLException e) {
            throw propagate(e);
        }
    }

    protected void after() {
        try {
            if (!connection.isClosed()) {
                connection.close();
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
