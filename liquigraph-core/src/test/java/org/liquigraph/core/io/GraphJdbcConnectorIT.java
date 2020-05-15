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
package org.liquigraph.core.io;

import java.sql.Connection;
import java.sql.SQLException;

import org.junit.Test;
import org.liquigraph.core.configuration.ConfigurationBuilder;
import org.liquigraph.core.io.lock.LockableConnection;
import org.liquigraph.testing.JdbcAwareGraphDatabase;
import org.liquigraph.testing.ParameterizedDatabaseIT;

import static org.assertj.core.api.Assertions.assertThat;

public class GraphJdbcConnectorIT extends ParameterizedDatabaseIT {

    public GraphJdbcConnectorIT(String description, JdbcAwareGraphDatabase graphDb, String uri) {
        super(description, graphDb, uri);
    }

    @Test
    public void locks_connection_to_database() throws SQLException {
        GraphJdbcConnector connector = new GraphJdbcConnector(new ConfigurationBuilder()
            .withRunMode()
            .withMasterChangelogLocation("changelog/changelog.xml")
            .withUri(uri)
            .withUsername(graphDb.username().orElse(null))
            .withPassword(graphDb.password().orElse(null))
            .build()
        );

        try (Connection connection = connector.connect()) {
            assertThat(connection).isInstanceOf(LockableConnection.class);
        }
    }

}
