package com.liquigraph.core.writer;

import com.liquigraph.core.configuration.ConfigurationBuilder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.neo4j.jdbc.internal.Neo4jConnection;

import java.sql.Connection;

import static org.assertj.core.api.Assertions.assertThat;

public class GraphJdbcConnectorTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private GraphJdbcConnector connector = new GraphJdbcConnector();

    @Test
    public void instantiates_a_remote_graph_database() {
        Connection connection = connector.connect(new ConfigurationBuilder()
                .withRunMode()
                .withMasterChangelogLocation("/changelog.xml")
                .withUri("jdbc:neo4j:mem")
                .build()
        );

        assertThat(connection).isInstanceOf(Neo4jConnection.class);
    }

}