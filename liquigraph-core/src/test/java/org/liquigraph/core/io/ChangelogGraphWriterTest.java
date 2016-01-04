/**
 * Copyright 2014-2016 the original author or authors.
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

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.liquigraph.core.EmbeddedGraphDatabaseRule;
import org.liquigraph.core.exception.PreconditionNotMetException;
import org.liquigraph.core.model.Changeset;
import org.liquigraph.core.model.Precondition;
import org.liquigraph.core.model.PreconditionErrorPolicy;
import org.liquigraph.core.model.SimpleQuery;
import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.Node;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.liquigraph.core.model.Checksums.checksum;

public class ChangelogGraphWriterTest {

    @Rule public EmbeddedGraphDatabaseRule graph = new EmbeddedGraphDatabaseRule("neotest");
    @Rule public ExpectedException thrown = ExpectedException.none();
    private ChangelogGraphWriter writer;

    @Before
    public void prepare() throws SQLException {
        writer = new ChangelogGraphWriter(graph.jdbcConnection(), new PreconditionExecutor());
    }

    @Test
    public void persists_changesets_in_graph() throws SQLException {
        writer.write(newArrayList(changeset("identifier", "fbiville", "CREATE (n: SomeNode {text:'yeah'})")));

        assertThatQueryIsExecutedAndHistoryPersisted(graph.jdbcConnection());
    }

    @Test
    public void persists_changesets_in_graph_when_preconditions_are_met() throws SQLException {
        Precondition precondition = precondition(PreconditionErrorPolicy.FAIL, "RETURN true AS result");
        Changeset changeset = changeset("identifier", "fbiville", "CREATE (n: SomeNode {text:'yeah'})", precondition);

        writer.write(newArrayList(changeset));

        assertThatQueryIsExecutedAndHistoryPersisted(graph.jdbcConnection());
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

            assertThat(resultSet.next()).as("No more result in result set").isFalse();
        }
    }

    @Test
    public void persists_changeset_but_does_execute_it_when_precondition_is_not_met_and_configured_to_mark_as_executed() throws SQLException {
        Precondition precondition = precondition(PreconditionErrorPolicy.MARK_AS_EXECUTED, "RETURN false AS result");
        Changeset changeset = changeset("identifier", "fbiville", "CREATE (n: SomeNode {text:'yeah'})", precondition);

        writer.write(singletonList(changeset));

        try (Statement statement = graph.jdbcConnection().createStatement();
             ResultSet resultSet = statement.executeQuery(
                 "OPTIONAL MATCH  (node: SomeNode) " +
                 "WITH node " +
                 "MATCH  (changelog:__LiquigraphChangelog)<-[ewc:EXECUTED_WITHIN_CHANGELOG]-(changeset:__LiquigraphChangeset), " +
                 "       (changeset)<-[:EXECUTED_WITHIN_CHANGESET]-(query:__LiquigraphQuery) " +
                 "RETURN ewc.time AS time, changeset, COLLECT(query.query) AS queries, node"
             )) {

            assertThat(resultSet.next()).as("Result set should contain 1 row").isTrue();
            assertThatChangesetIsStored(resultSet);
            assertThatQueryIsNotExecuted(resultSet);
            assertThat(resultSet.next()).as("No more result in result set").isFalse();
        }
    }

    @Test
    public void executes_changeset_with_multiple_queries() throws SQLException {
        Changeset changeset = changeset(
           "id",
           "fbiville",
           asList(
              "CREATE (n:Human) RETURN n",
              "MATCH (n:Human) SET n.age = 42 RETURN n")
        );

        writer.write(singletonList(changeset));

        try (Statement transaction = graph.jdbcConnection().createStatement();
             ResultSet resultSet = transaction.executeQuery("MATCH (n:Human) RETURN n.age AS age")) {

            assertThat(resultSet.next()).isTrue();
            assertThat(resultSet.getLong("age")).isEqualTo(42);
            assertThat(resultSet.next()).as("No more result in result set").isFalse();
        }

        try (Statement transaction = graph.jdbcConnection().createStatement();
             ResultSet resultSet = transaction.executeQuery("MATCH (queries:__LiquigraphQuery) RETURN COLLECT(queries.query) AS query")) {

            assertThat(resultSet.next()).isTrue();
            assertThat((Collection<String>)resultSet.getObject("query")).containsExactly(
                "CREATE (n:Human) RETURN n", "MATCH (n:Human) SET n.age = 42 RETURN n"
            );
            assertThat(resultSet.next()).as("No more result in result set").isFalse();
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
        Collection<String> queries = singletonList(query);
        return changeset(identifier, author, queries);
    }

    private Changeset changeset(String identifier, String author, Collection<String> queries) {
        Changeset changeset = new Changeset();
        changeset.setId(identifier);
        changeset.setAuthor(author);
        changeset.setQueries(queries);
        return changeset;
    }

    private static void assertThatQueryIsExecutedAndHistoryPersisted(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(
                 "MATCH  (node: SomeNode), " +
                     "       (changelog:__LiquigraphChangelog)<-[ewc:EXECUTED_WITHIN_CHANGELOG]-(changeset:__LiquigraphChangeset), " +
                     "       (changeset)<-[:EXECUTED_WITHIN_CHANGESET]-(query:__LiquigraphQuery) " +
                     "RETURN ewc.time AS time, changeset, COLLECT(query.query) AS queries, node"
             )) {

            assertThat(resultSet.next()).as("Result set should contain 1 row").isTrue();
            assertThatChangesetIsStored(resultSet);
            assertThatQueryIsExecuted(resultSet);
            assertThat(resultSet.next()).as("No more result in result set").isFalse();
        }
    }

    private static void assertThatChangesetIsStored(ResultSet resultSet) throws SQLException {
        assertThat(resultSet.getLong("time")).isGreaterThan(0);

        assertThat((Collection<String>) resultSet.getObject("queries"))
            .containsExactly("CREATE (n: SomeNode {text:'yeah'})");

        Node changeset = (Node) resultSet.getObject("changeset");
        assertThat(changeset.getProperty("id")).isEqualTo("identifier");
        assertThat(changeset.getProperty("author")).isEqualTo("fbiville");
        assertThat(changeset.getProperty("checksum")).isEqualTo(checksum(singletonList("CREATE (n: SomeNode {text:'yeah'})")));
    }

    private static void assertThatQueryIsExecuted(ResultSet resultSet) throws SQLException {
        Node node = (Node) resultSet.getObject("node");
        assertThat(node.getLabels()).containsExactly(DynamicLabel.label("SomeNode"));
        assertThat(node.getProperty("text")).isEqualTo("yeah");
    }

    private static void assertThatQueryIsNotExecuted(ResultSet resultSet) throws SQLException {
        assertThat(resultSet.getObject("node")).isNull();
    }
}