package org.liquigraph.core.writer;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.liquigraph.core.exception.PreconditionNotMetException;
import org.liquigraph.core.model.Changeset;
import org.liquigraph.core.model.Precondition;
import org.liquigraph.core.model.PreconditionErrorPolicy;
import org.liquigraph.core.model.SimpleQuery;
import org.liquigraph.core.rules.EmbeddedGraphDatabaseRule;
import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.Node;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.liquigraph.core.model.Checksums.checksum;

public class ChangelogGraphWriterTest {

    @Rule
    public EmbeddedGraphDatabaseRule graph = new EmbeddedGraphDatabaseRule("neotest");

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private ChangelogGraphWriter writer;

    @Before
    public void prepare() throws SQLException {
        writer = new ChangelogGraphWriter(graph.jdbcConnection(), new PreconditionExecutor());
    }

    @Test
    public void persists_changesets_in_graph() throws SQLException {

        writer.write(newArrayList(changeset("identifier", "fbiville", "CREATE (n: SomeNode {text:'yeah'})")));

        try (Statement statement = graph.jdbcConnection().createStatement();
            ResultSet resultSet = statement.executeQuery(
                "MATCH (node: SomeNode), (changelog:__LiquigraphChangelog)<-[execution:EXECUTED_WITHIN_CHANGELOG]-(changeset:__LiquigraphChangeset) " +
                "RETURN execution.order AS order, changeset, node"
            )) {

            assertThat(resultSet.next()).isTrue();

            assertThat(resultSet.getLong("order")).isEqualTo(1L);
            Node changeset = (Node) resultSet.getObject("changeset");
            assertThat(changeset.getProperty("id")).isEqualTo("identifier");
            assertThat(changeset.getProperty("author")).isEqualTo("fbiville");
            assertThat(changeset.getProperty("query")).isEqualTo("CREATE (n: SomeNode {text:'yeah'})");
            assertThat(changeset.getProperty("checksum")).isEqualTo(checksum("CREATE (n: SomeNode {text:'yeah'})"));
            Node node = (Node) resultSet.getObject("node");
            assertThat(node.getLabels()).containsExactly(DynamicLabel.label("SomeNode"));
            assertThat(node.getProperty("text")).isEqualTo("yeah");

            assertThat(resultSet.next()).isFalse();
        }
    }

    @Test
    public void persists_changesets_in_graph_when_preconditions_are_met() throws SQLException {
        Precondition precondition = precondition(PreconditionErrorPolicy.FAIL, "RETURN true AS result");
        Changeset changeset = changeset("identifier", "fbiville", "CREATE (n: SomeNode {text:'yeah'})", precondition);

        writer.write(newArrayList(changeset));

        try (Statement statement = graph.jdbcConnection().createStatement();
             ResultSet resultSet = statement.executeQuery(
                 "MATCH (node: SomeNode), (changelog:__LiquigraphChangelog)<-[execution:EXECUTED_WITHIN_CHANGELOG]-(changeset:__LiquigraphChangeset) " +
                 "RETURN execution.order AS order, changeset, node"
             )) {

            assertThat(resultSet.next()).isTrue();

            assertThat(resultSet.getLong("order")).isEqualTo(1L);
            Node persistedChangeset = (Node) resultSet.getObject("changeset");
            assertThat(persistedChangeset.getProperty("id")).isEqualTo("identifier");
            assertThat(persistedChangeset.getProperty("author")).isEqualTo("fbiville");
            assertThat(persistedChangeset.getProperty("query")).isEqualTo("CREATE (n: SomeNode {text:'yeah'})");
            assertThat(persistedChangeset.getProperty("checksum")).isEqualTo(checksum("CREATE (n: SomeNode {text:'yeah'})"));
            Node node = (Node) resultSet.getObject("node");
            assertThat(node.getLabels()).containsExactly(DynamicLabel.label("SomeNode"));
            assertThat(node.getProperty("text")).isEqualTo("yeah");

            assertThat(resultSet.next()).isFalse();
        }
    }

    @Test
    public void fails_when_precondition_is_not_met_and_configured_to_halt_execution() {
        thrown.expect(PreconditionNotMetException.class);
        thrown.expectMessage("Changeset <identifier>: precondition query <RETURN false AS result> failed with policy <FAIL>. Aborting.");

        Precondition precondition = precondition(PreconditionErrorPolicy.FAIL, "RETURN false AS result");
        Changeset changeset = changeset("identifier", "fbiville", "CREATE (n: SomeNode {text:'yeah'})", precondition);

        writer.write(newArrayList(changeset));
    }

    @Test
    public void skips_changeset_execution_when_precondition_is_not_met_and_configured_to_skip() throws SQLException {
        Precondition precondition = precondition(PreconditionErrorPolicy.CONTINUE, "RETURN false AS result");
        Changeset changeset = changeset("identifier", "fbiville", "CREATE (n: SomeNode {text:'yeah'})", precondition);

        writer.write(newArrayList(changeset));

        try (Statement statement = graph.jdbcConnection().createStatement();
             ResultSet resultSet = statement.executeQuery(
                 "MATCH (changelog:__LiquigraphChangelog)<-[execution:EXECUTED_WITHIN_CHANGELOG]-(changeset:__LiquigraphChangeset) " +
                 "OPTIONAL MATCH (node :SomeNode) " +
                 "RETURN execution.order AS order, changeset, node"
             )) {

            assertThat(resultSet.next()).isFalse();
        }
    }

    @Test
    public void persists_changeset_but_does_execute_it_when_precondition_is_not_met_and_configured_to_mark_as_executed() throws SQLException {
        Precondition precondition = precondition(PreconditionErrorPolicy.MARK_AS_EXECUTED, "RETURN false AS result");
        Changeset changeset = changeset("identifier", "fbiville", "CREATE (n: SomeNode {text:'yeah'})", precondition);

        writer.write(newArrayList(changeset));

        try (Statement transaction = graph.jdbcConnection().createStatement();
             ResultSet resultSet = transaction.executeQuery(
                 "MATCH (changelog:__LiquigraphChangelog)<-[execution:EXECUTED_WITHIN_CHANGELOG]-(changeset:__LiquigraphChangeset) " +
                 "OPTIONAL MATCH (node:SomeNode)" +
                 "RETURN execution.order AS order, changeset, node"
             )) {

            assertThat(resultSet.next()).isTrue();

            assertThat(resultSet.getLong("order")).isEqualTo(1L);
            Node persistedChangeset = (Node) resultSet.getObject("changeset");
            assertThat(persistedChangeset.getProperty("id")).isEqualTo("identifier");
            assertThat(persistedChangeset.getProperty("author")).isEqualTo("fbiville");
            assertThat(persistedChangeset.getProperty("query")).isEqualTo("CREATE (n: SomeNode {text:'yeah'})");
            assertThat(persistedChangeset.getProperty("checksum")).isEqualTo(checksum("CREATE (n: SomeNode {text:'yeah'})"));
            assertThat(resultSet.getObject("node")).isNull();

            assertThat(resultSet.next()).isFalse();
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