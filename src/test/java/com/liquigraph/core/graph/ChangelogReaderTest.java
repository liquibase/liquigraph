package com.liquigraph.core.graph;

import com.liquigraph.core.model.Changeset;
import com.liquigraph.core.rules.EmbeddedGraphDatabaseRule;
import org.junit.Rule;
import org.junit.Test;
import org.neo4j.graphdb.Transaction;

import java.util.Collection;

import static com.liquigraph.core.model.Checksums.checksum;
import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;

public class ChangelogReaderTest {

    @Rule
    public EmbeddedGraphDatabaseRule graph = new EmbeddedGraphDatabaseRule();

    private ChangelogReader reader = new ChangelogReader();

    @Test
    public void reads_changelog_from_graph_database() {
        given_inserted_data(format(
            "CREATE (:__LiquigraphChangelog)<-[:EXECUTED_WITHIN_CHANGELOG {order:1}]-" +
                "(:__LiquigraphChangeset {" +
                "   author:'fbiville'," +
                "   id:'test'," +
                "   query:'%s', " +
                "   checksum:'%s'" +
                "})"
            , "MATCH n RETURN n", checksum("MATCH n RETURN n")));

        Collection<Changeset> changesets = reader.read(graph.graphDatabase());

        assertThat(changesets).extracting("id", "author", "query", "checksum").containsExactly(
            tuple("test", "fbiville", "MATCH n RETURN n", checksum("MATCH n RETURN n"))
        );
    }

    private void given_inserted_data(String query) {
        try (Transaction transaction = graph.graphDatabase().beginTx()) {
            graph.cypherEngine().execute(query);
            transaction.success();
        }
    }

}