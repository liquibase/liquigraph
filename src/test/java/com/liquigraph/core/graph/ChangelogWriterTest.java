package com.liquigraph.core.graph;

import com.liquigraph.core.exception.PreconditionNotMetException;
import com.liquigraph.core.model.Changeset;
import com.liquigraph.core.model.Precondition;
import com.liquigraph.core.model.PreconditionErrorPolicy;
import com.liquigraph.core.model.SimpleQuery;
import com.liquigraph.core.rules.EmbeddedGraphDatabaseRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.neo4j.graphdb.DynamicLabel;
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

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private ChangelogWriter writer = new ChangelogWriter();

    @Test
    public void persists_changesets_in_graph() {
        writer.write(
            graph.graphDatabase(),
            new PreconditionExecutor(),
            newArrayList(changeset("identifier", "fbiville", "CREATE (n: SomeNode {text:'yeah'})"))
        );

        try (Transaction transaction = graph.graphDatabase().beginTx();
            ResourceIterator<Map<String, Object>> iterator = graph.cypherEngine().execute(
                "MATCH (node: SomeNode), (changelog:__LiquigraphChangelog)<-[execution:EXECUTED_WITHIN_CHANGELOG]-(changeset:__LiquigraphChangeset) " +
                    "RETURN execution.order AS order, changeset, node"
            ).iterator()) {

            Map<String, Object> columns = iterator.next();
            assertThat(columns.get("order")).isEqualTo(1L);
            Node changeset = (Node) columns.get("changeset");
            assertThat(changeset.getProperty("id")).isEqualTo("identifier");
            assertThat(changeset.getProperty("author")).isEqualTo("fbiville");
            assertThat(changeset.getProperty("query")).isEqualTo("CREATE (n: SomeNode {text:'yeah'})");
            assertThat(changeset.getProperty("checksum")).isEqualTo(checksum("CREATE (n: SomeNode {text:'yeah'})"));
            Node node = (Node) columns.get("node");
            assertThat(node.getLabels()).containsExactly(DynamicLabel.label("SomeNode"));
            assertThat(node.getProperty("text")).isEqualTo("yeah");
            assertThat(iterator.hasNext()).isFalse();
            
            transaction.success();
        }
    }

    @Test
    public void persists_changesets_in_graph_when_preconditions_are_met() {
        Precondition precondition = precondition(PreconditionErrorPolicy.FAIL, "RETURN true AS result");
        Changeset changeset = changeset("identifier", "fbiville", "CREATE (n: SomeNode {text:'yeah'})", precondition);

        writer.write(
            graph.graphDatabase(),
            new PreconditionExecutor(),
            newArrayList(changeset)
        );

        try (Transaction transaction = graph.graphDatabase().beginTx();
             ResourceIterator<Map<String, Object>> iterator = graph.cypherEngine().execute(
                 "MATCH (node: SomeNode), (changelog:__LiquigraphChangelog)<-[execution:EXECUTED_WITHIN_CHANGELOG]-(changeset:__LiquigraphChangeset) " +
                     "RETURN execution.order AS order, changeset, node"
             ).iterator()) {

            Map<String, Object> columns = iterator.next();
            assertThat(columns.get("order")).isEqualTo(1L);
            Node persistedChangeset = (Node) columns.get("changeset");
            assertThat(persistedChangeset.getProperty("id")).isEqualTo("identifier");
            assertThat(persistedChangeset.getProperty("author")).isEqualTo("fbiville");
            assertThat(persistedChangeset.getProperty("query")).isEqualTo("CREATE (n: SomeNode {text:'yeah'})");
            assertThat(persistedChangeset.getProperty("checksum")).isEqualTo(checksum("CREATE (n: SomeNode {text:'yeah'})"));
            Node node = (Node) columns.get("node");
            assertThat(node.getLabels()).containsExactly(DynamicLabel.label("SomeNode"));
            assertThat(node.getProperty("text")).isEqualTo("yeah");
            assertThat(iterator.hasNext()).isFalse();

            transaction.success();
        }
    }

    @Test
    public void fails_when_precondition_is_not_met_and_configured_to_halt_execution() {
        thrown.expect(PreconditionNotMetException.class);
        thrown.expectMessage("Changeset <identifier>: precondition query <RETURN false AS result> failed with policy <FAIL>. Aborting.");

        Precondition precondition = precondition(PreconditionErrorPolicy.FAIL, "RETURN false AS result");
        Changeset changeset = changeset("identifier", "fbiville", "CREATE (n: SomeNode {text:'yeah'})", precondition);

        writer.write(
            graph.graphDatabase(),
            new PreconditionExecutor(),
            newArrayList(changeset)
        );
    }

    @Test
    public void skips_changeset_execution_when_precondition_is_not_met_and_configured_to_skip() {
        Precondition precondition = precondition(PreconditionErrorPolicy.CONTINUE, "RETURN false AS result");
        Changeset changeset = changeset("identifier", "fbiville", "CREATE (n: SomeNode {text:'yeah'})", precondition);

        writer.write(
            graph.graphDatabase(),
            new PreconditionExecutor(),
            newArrayList(changeset)
        );

        try (Transaction transaction = graph.graphDatabase().beginTx();
             ResourceIterator<Map<String, Object>> iterator = graph.cypherEngine().execute(
                 "MATCH (changelog:__LiquigraphChangelog)<-[execution:EXECUTED_WITHIN_CHANGELOG]-(changeset:__LiquigraphChangeset) " +
                 "OPTIONAL MATCH (node :SomeNode) " +
                 "RETURN execution.order AS order, changeset, node"
             ).iterator()) {

            assertThat(iterator).isEmpty();
            transaction.success();
        }
    }

    @Test
    public void persists_changeset_but_does_execute_it_when_precondition_is_not_met_and_configured_to_mark_as_executed() {
        Precondition precondition = precondition(PreconditionErrorPolicy.MARK_AS_EXECUTED, "RETURN false AS result");
        Changeset changeset = changeset("identifier", "fbiville", "CREATE (n: SomeNode {text:'yeah'})", precondition);

        writer.write(
            graph.graphDatabase(),
            new PreconditionExecutor(),
            newArrayList(changeset)
        );

        try (Transaction transaction = graph.graphDatabase().beginTx();
             ResourceIterator<Map<String, Object>> iterator = graph.cypherEngine().execute(
                 "MATCH (changelog:__LiquigraphChangelog)<-[execution:EXECUTED_WITHIN_CHANGELOG]-(changeset:__LiquigraphChangeset) " +
                 "OPTIONAL MATCH (node :SomeNode)" +
                 "RETURN execution.order AS order, changeset, node"
             ).iterator()) {

            Map<String, Object> columns = iterator.next();
            assertThat(columns.get("order")).isEqualTo(1L);
            Node persistedChangeset = (Node) columns.get("changeset");
            assertThat(persistedChangeset.getProperty("id")).isEqualTo("identifier");
            assertThat(persistedChangeset.getProperty("author")).isEqualTo("fbiville");
            assertThat(persistedChangeset.getProperty("query")).isEqualTo("CREATE (n: SomeNode {text:'yeah'})");
            assertThat(persistedChangeset.getProperty("checksum")).isEqualTo(checksum("CREATE (n: SomeNode {text:'yeah'})"));
            assertThat(columns.get("node")).isNull();
            assertThat(iterator.hasNext()).isFalse();

            transaction.success();
        }
    }

    private Precondition precondition(PreconditionErrorPolicy policy, String query) {
        Precondition precondition = new Precondition();
        precondition.setPolicy(policy);
        SimpleQuery simpleQuery = new SimpleQuery();
        simpleQuery.setQuery(query);
        precondition.setQuery(simpleQuery);
        return precondition;
    }

    private Changeset changeset(String identifier, String author, String query, Precondition precondition) {
        Changeset changeset = changeset(identifier, author, query);
        changeset.setPrecondition(precondition);
        return changeset;
    }

    private Changeset changeset(String identifier, String author, String query) {
        Changeset changeset = new Changeset();
        changeset.setId(identifier);
        changeset.setAuthor(author);
        changeset.setQuery(query);
        return changeset;
    }

}