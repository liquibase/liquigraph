package com.liquigraph.core.writer;

import com.liquigraph.core.configuration.ConfigurationBuilder;
import org.jboss.byteman.contrib.bmunit.BMRule;
import org.jboss.byteman.contrib.bmunit.BMUnitRunner;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;
import org.neo4j.rest.graphdb.RestGraphDatabase;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(BMUnitRunner.class)
public class GraphConnectorTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    private GraphConnector connector = new GraphConnector();

    @Test
    public void instantiates_an_embedded_database() throws IOException {
        GraphDatabaseService graphDatabaseService = connector.connect(new ConfigurationBuilder()
                .withRunMode()
                .withMasterChangelogLocation("/changelog.xml")
                .withUri(folder.newFolder().toURI().toString())
                .build()
        );

        try (Transaction transaction = graphDatabaseService.beginTx()) {
            assertThat(graphDatabaseService.getAllNodes()).isEmpty();
            transaction.success();
        }
    }

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

    @Test
    public void fails_to_instantiate_embedded_database_service_for_invalid_file_location() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("'uri' points to an invalid location for embedded graph database. Given: <file:/I.will.fail>");

        connector.connect(new ConfigurationBuilder()
                .withRunMode()
                .withMasterChangelogLocation("/changelog.xml")
                .withUri("file:/I.will.fail")
                .build()
        );
    }

    @BMRule(
        name = "forces URI syntax error",
        targetClass = "GraphConnector",
        targetMethod = "getPath(String)", // yep, it is a bit fragile
        action = "throw new URISyntaxException(\"file://fake\",\"bazinga\")"
    )
    @Test
    public void fails_to_instantiate_embedded_database_service_for_invalid_file_uri_syntax() throws IOException {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("'uri' is invalid for embedded graph database.");

        connector.connect(new ConfigurationBuilder()
                .withRunMode()
                .withMasterChangelogLocation("/changelog.xml")
                .withUri(folder.newFolder().toURI().toString())
                .build()
        );
    }

}