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
package org.liquigraph.core.configuration;

import org.neo4j.graphdb.GraphDatabaseService;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.UUID;

import static com.google.common.base.Throwables.propagate;

class ConnectionConfigurationByGraphDatabaseservice implements ConnectionConfiguration {

    private final String uri;
    private final Properties driverProperties;

    public ConnectionConfigurationByGraphDatabaseservice(GraphDatabaseService graphDatabaseService) {

        // databaseName is what the driver uses to link the URI to the
        // GraphDatabaseService instance.
        String databaseName = generateUniqueDatabaseName();

        uri = String.format("jdbc:neo4j:instance:%s", databaseName);

        driverProperties = new Properties();
        driverProperties.put(databaseName, graphDatabaseService);
    }

    private String generateUniqueDatabaseName() {

        return UUID.randomUUID().toString();
    }

    @Override
    public Connection get() {

        return getConnection(uri, driverProperties);
    }

    protected Connection getConnection(String uri, Properties driverProperties) {
        try {
            return DriverManager.getConnection(uri, driverProperties);
        } catch (SQLException e) {
            throw propagate(e);
        }
    }
}