package com.liquigraph.core.writer;

import com.liquigraph.core.configuration.ConfigurationBuilder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.rest.graphdb.RestGraphDatabase;

import static org.assertj.core.api.Assertions.assertThat;

public class GraphConnectorTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private GraphConnector connector = new GraphConnector();

    @Test
    public void instantiates_a_remote_graph_database() {
        GraphDatabaseService graphDatabase = connector.connect(new ConfigurationBuilder()
                .withRunMode()
                .withMasterChangelogLocation("/changelog.xml")
                .withUri("http://localhost:8082")
                .build()
        );

        assertThat(graphDatabase).isInstanceOf(RestGraphDatabase.class);
    }

    @Test
    public void instantiates_a_remote_database_with_credentials() {
        GraphDatabaseService graphDatabase = connector.connect(new ConfigurationBuilder()
                .withRunMode()
                .withMasterChangelogLocation("/changelog.xml")
                .withUri("http://localhost:8082")
                .withUsername("fbiville")
                .withPassword("secret")
                .build()
        );

        assertThat(graphDatabase).isInstanceOf(RestGraphDatabase.class);
    }

}