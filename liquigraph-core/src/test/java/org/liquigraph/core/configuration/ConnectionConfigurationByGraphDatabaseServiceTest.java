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

import org.junit.Test;
import org.neo4j.graphdb.GraphDatabaseService;

import java.sql.Connection;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class ConnectionConfigurationByGraphDatabaseServiceTest {

    @Test
    public void connects_via_the_provided_database() {

        Connection expectedConnection = mock(Connection.class);
        GraphDatabaseService expectedDatabase = mock(GraphDatabaseService.class);

        TestConfiguration configuration = new TestConfiguration(
                expectedDatabase,
                expectedConnection);

        Connection connection = configuration.get();
        assertThat(connection).isSameAs(expectedConnection);

        String databaseName = extractDatabaseNameFromConnectionUri(configuration.getUri());

        Properties driverProperties = configuration.getDriverProperties();
        Object propertyValue = driverProperties.get(databaseName);
        assertThat(propertyValue).isSameAs(expectedDatabase);
    }

    /**
     * Extracts database name from the provided connection URI.
     * @param uri Expected format is "jdbc:neo4j:instance:databaseName".
     * @return the database name.
     */
    private String extractDatabaseNameFromConnectionUri(String uri) {

        assertThat(uri)
                .isNotNull()
                .isNotEmpty()
                .contains(":");

        String[] splittedUri = uri.split(":");

        return splittedUri[splittedUri.length - 1];
    }

    private static class TestConfiguration extends ConnectionConfigurationByGraphDatabaseService {

        private final Connection connection;
        private String uri;
        private Properties driverProperties;

        public TestConfiguration(GraphDatabaseService graphDatabaseService, Connection connection) {
            super(graphDatabaseService);

            this.connection = connection;
        }

        @Override
        protected Connection getConnection(String uri, Properties driverProperties) {

            this.uri = uri;
            this.driverProperties = driverProperties;

            return connection;
        }

        public String getUri() {
            return uri;
        }

        public Properties getDriverProperties() {
            return driverProperties;
        }
    }
}
