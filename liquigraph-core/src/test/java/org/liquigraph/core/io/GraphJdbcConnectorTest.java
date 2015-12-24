package org.liquigraph.core.io;

import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.liquigraph.core.configuration.ConfigurationBuilder;
import org.neo4j.jdbc.internal.Neo4jConnection;

import java.sql.Connection;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.MapEntry.entry;

public class GraphJdbcConnectorTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private GraphJdbcConnector connector = new GraphJdbcConnector();

    @Test
    public void instantiates_a_local_graph_database() {
        Connection connection = connector.connect(new ConfigurationBuilder()
                .withRunMode()
                .withMasterChangelogLocation("changelog/changelog.xml")
                .withUri("jdbc:neo4j:mem")
                .build()
        );

        assertThat(connection).isInstanceOf(Neo4jConnection.class);
    }

    @Test
    @Ignore("requires starting local Neo4j instance")
    public void instantiates_a_remote_graph_database() {
        Connection connection = connector.connect(new ConfigurationBuilder()
            .withRunMode()
            .withMasterChangelogLocation("changelog.xml")
            .withUri("jdbc:neo4j://localhost:7474")
            .withUsername("neo4j")
            .withPassword("toto")
            .build()
        );

        assertThat(((Neo4jConnection) connection).getProperties()).contains(
            entry("user", "neo4j"),
            entry("password", "toto")
        );
    }

}