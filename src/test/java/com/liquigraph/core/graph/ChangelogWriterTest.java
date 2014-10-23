package com.liquigraph.core.graph;

import com.liquigraph.core.model.Changeset;
import com.liquigraph.core.rules.EmbeddedGraphDatabaseRule;
import org.junit.Rule;
import org.junit.Test;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.ResourceIterator;
import org.neo4j.graphdb.Transaction;

import java.util.Map;

import static com.google.common.collect.Lists.newArrayList;
import static com.liquigraph.core.model.Checksums.checksum;
import static org.assertj.core.api.Assertions.assertThat;

public class ChangelogWriterTest {

    @Rule
    public EmbeddedGraphDatabaseRule graph = new EmbeddedGraphDatabaseRule();

    private ChangelogWriter writer = new ChangelogWriter();

    @Test
    public void persists_changesets_in_graph() {
        writer.write(graph.graphDatabase(), newArrayList(changeset("identifier", "fbiville", "CREATE n")));

        try (Transaction transaction = graph.graphDatabase().beginTx();
            ResourceIterator<Map<String, Object>> iterator = graph.cypherEngine().execute(
                "MATCH (changelog:__LiquigraphChangelog)<-[execution:EXECUTED_WITHIN_CHANGELOG]-(changeset:__LiquigraphChangeset) " +
                    "RETURN execution.order AS order, changeset"
            ).iterator()) {

            Map<String, Object> columns = iterator.next();
            assertThat(columns.get("order")).isEqualTo(1L);
            Node changeset = (Node) columns.get("changeset");
            assertThat(changeset.getProperty("id")).isEqualTo("identifier");
            assertThat(changeset.getProperty("author")).isEqualTo("fbiville");
            assertThat(changeset.getProperty("query")).isEqualTo("CREATE n");
            assertThat(changeset.getProperty("checksum")).isEqualTo(checksum("CREATE n"));
            assertThat(iterator.hasNext()).isFalse();
            
            transaction.success();
        }
    }

    private Changeset changeset(String identifier, String author, String query) {
        Changeset changeset = new Changeset();
        changeset.setId(identifier);
        changeset.setAuthor(author);
        changeset.setQuery(query);
        return changeset;
    }

}