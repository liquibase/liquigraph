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
package org.liquigraph.core.io;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.liquigraph.core.configuration.ConfigurationBuilder;
import org.liquigraph.core.io.lock.LockableConnection;
import org.neo4j.jdbc.internal.Neo4jConnection;

import java.sql.Connection;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.MapEntry.entry;
import static org.liquigraph.core.RemoteGraphDatabaseRule.assumeRemoteGraphDatabaseIsProvisioned;

public class GraphJdbcConnectorTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private GraphJdbcConnector connector = new GraphJdbcConnector();

    @Test
    public void instantiates_a_local_graph_database() throws SQLException {
        try (Connection connection = connector.connect(new ConfigurationBuilder()
            .withRunMode()
            .withMasterChangelogLocation("changelog/changelog.xml")
            .withUri("jdbc:neo4j:mem")
            .build()
        )) {
            assertThat(connection).isInstanceOf(LockableConnection.class);
        }

    }

    @Test
    public void instantiates_a_remote_graph_database() throws SQLException {
        assumeRemoteGraphDatabaseIsProvisioned();

        try (Connection connection = connector.connect(new ConfigurationBuilder()
            .withRunMode()
            .withMasterChangelogLocation("changelog/changelog.xml")
            .withUri("jdbc:neo4j://127.0.0.1:7474")
            .withUsername("neo4j")
            .withPassword("j4oen")
            .build()
        )) {
            assertThat(connection).isInstanceOf(LockableConnection.class);
        }
    }

}