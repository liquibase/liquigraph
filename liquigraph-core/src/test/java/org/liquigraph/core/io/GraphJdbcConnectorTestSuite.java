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
package org.liquigraph.core.io;

import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.liquigraph.core.GraphIntegrationTestSuite;
import org.liquigraph.core.configuration.ConfigurationBuilder;
import org.liquigraph.core.io.lock.LockableConnection;

import java.sql.Connection;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;

public abstract class GraphJdbcConnectorTestSuite implements GraphIntegrationTestSuite {

    @Rule public ExpectedException thrown = ExpectedException.none();

    protected final GraphJdbcConnector connector = new GraphJdbcConnector();

    @Ignore("TODO: This scheme isn't supported any longer")
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
        try (Connection connection = connector.connect(new ConfigurationBuilder()
            .withRunMode()
            .withMasterChangelogLocation("changelog/changelog.xml")
            .withUri(graphDatabase().uri())
            .withUsername(graphDatabase().username().orNull())
            .withPassword(graphDatabase().password().orNull())
            .build()
        )) {
            assertThat(connection).isInstanceOf(LockableConnection.class);
        }
    }

}
