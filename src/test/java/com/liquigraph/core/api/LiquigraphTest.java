package com.liquigraph.core.api;

import com.liquigraph.core.configuration.ConfigurationBuilder;
import com.liquigraph.core.rules.EmbeddedGraphDatabaseRule;
import org.junit.Rule;
import org.junit.Test;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.ResourceIterable;
import org.neo4j.graphdb.Transaction;

import static org.assertj.core.api.Assertions.assertThat;
import static org.neo4j.graphdb.DynamicLabel.label;

public class LiquigraphTest {

    @Rule
    public EmbeddedGraphDatabaseRule embeddedGraphDb = new EmbeddedGraphDatabaseRule();

    private Liquigraph liquigraph = new Liquigraph();

    @Test
    public void runs_migrations_against_embedded_graph_with_failed_precondition_whose_changeset_is_marked_as_executed() {
        GraphDatabaseService graphDb = embeddedGraphDb.graphDatabase();
        liquigraph.runMigrations(
            new ConfigurationBuilder()
                .withRunMode()
                .withMasterChangelogLocation("/changelog-with-1-node.xml")
                .withGraphDatabaseService(graphDb)
                .build()
        );

        try (Transaction transaction = graphDb.beginTx()) {
            ResourceIterable<Node> people = graphDb.findNodesByLabelAndProperty(label("Human"), "name", "fbiville");
            assertThat(people).hasSize(1);

            ResourceIterable<Node> firstChangeset = graphDb.findNodesByLabelAndProperty(
                label("__LiquigraphChangeset"), "id", "insert-fbiville"
            );
            assertThat(firstChangeset).as("no changeset inserted").hasSize(1);

            ResourceIterable<Node> secondChangeset = graphDb.findNodesByLabelAndProperty(
                label("__LiquigraphChangeset"), "id", "insert-fbiville-again"
            );
            assertThat(secondChangeset).as("no changeset marked as executed inserted").hasSize(1);
            transaction.success();
        }
    }

}