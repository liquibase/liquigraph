/*
 * Copyright 2014-2018 the original author or authors.
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

import com.google.common.base.Supplier;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.liquigraph.core.GraphIntegrationTestSuite;
import org.liquigraph.core.exception.PreconditionNotMetException;
import org.liquigraph.core.model.Changeset;
import org.liquigraph.core.model.Postcondition;
import org.liquigraph.core.model.Precondition;
import org.liquigraph.core.model.PreconditionErrorPolicy;
import org.liquigraph.core.model.SimpleQuery;
import org.neo4j.graphdb.Node;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.Map;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.failBecauseExceptionWasNotThrown;
import static org.liquigraph.core.model.Checksums.checksum;

abstract class ChangelogGraphWriterTestSuite implements GraphIntegrationTestSuite {

    private ConnectionSupplier connectionSupplier = new ConnectionSupplier();
    private ChangelogGraphWriter writer;
    private Connection connection;

    @Before
    public void prepare() throws SQLException {
        connection = connectionSupplier.get();
        writer = new ChangelogGraphWriter(connection, connectionSupplier, new ConditionExecutor());
    }

    @After
    public void close() throws SQLException {
        connection.close();
    }

    @Test
    public void persists_changesets_in_graph() throws SQLException {
        writer.write(newArrayList(changeset("identifier", "fbiville", "CREATE (n: SomeNode {text:'yeah'})")));

        assertThatQueryIsExecutedAndHistoryPersisted();
    }

    @Test
    public void persists_changesets_in_graph_when_preconditions_are_met() throws SQLException {
        Precondition precondition = precondition(PreconditionErrorPolicy.FAIL, "RETURN true AS result");
        Changeset changeset = changeset("identifier", "fbiville", "CREATE (n: SomeNode {text:'yeah'})", precondition);

        writer.write(newArrayList(changeset));

        assertThatQueryIsExecutedAndHistoryPersisted();
    }

    @Test
    public void fails_when_precondition_is_not_met_and_configured_to_halt_execution() {
        try {
            Precondition precondition = precondition(PreconditionErrorPolicy.FAIL, "RETURN false AS result");
            Changeset changeset = changeset("identifier", "fbiville", "CREATE (n: SomeNode {text:'yeah'})", precondition);

            writer.write(newArrayList(changeset));
            failBecauseExceptionWasNotThrown(PreconditionNotMetException.class);
        }
        catch (PreconditionNotMetException pnme) {
            assertThat(pnme)
                .hasMessage("Changeset id=<identifier>, author=<fbiville>: precondition query <RETURN false AS result> failed with policy <FAIL>. Aborting.");
        }

    }

    @Test
    public void skips_changeset_execution_when_precondition_is_not_met_and_configured_to_skip() throws SQLException {
        Precondition precondition = precondition(PreconditionErrorPolicy.CONTINUE, "RETURN false AS result");
        Changeset changeset = changeset("identifier", "fbiville", "CREATE (n: SomeNode {text:'yeah'})", precondition);

        writer.write(newArrayList(changeset));

        try (Connection connection = graphDatabase().newConnection();
             Statement statement = connection.createStatement();
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

        try (Connection connection = graphDatabase().newConnection();
             Statement statement = connection.createStatement();
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

        try (Connection connection = graphDatabase().newConnection();
             Statement transaction = connection.createStatement();
             ResultSet resultSet = transaction.executeQuery("MATCH (n:Human) RETURN n.age AS age")) {
            assertThat(resultSet.next()).isTrue();
            assertThat(resultSet.getLong("age")).isEqualTo(42);
            assertThat(resultSet.next()).as("No more result in result set").isFalse();
        }

        try (Connection connection = graphDatabase().newConnection();
             Statement transaction = connection.createStatement();
             ResultSet resultSet = transaction.executeQuery("MATCH (queries:__LiquigraphQuery) RETURN COLLECT(queries.query) AS query")) {
            assertThat(resultSet.next()).isTrue();
            assertThat((Collection<String>)resultSet.getObject("query")).containsExactly(
                "CREATE (n:Human) RETURN n", "MATCH (n:Human) SET n.age = 42 RETURN n"
            );
            assertThat(resultSet.next()).as("No more result in result set").isFalse();
        }
    }

    @Test
    public void persists_run_always_changesets_in_graph_only_once() throws SQLException {
        Changeset changeset = changeset("identifier", "fbiville",
                "MERGE (n:SomeNode) " +
                "ON CREATE SET n.prop = 1 " +
                "ON MATCH SET n.prop = n.prop + 1");
        changeset.setRunAlways(true);

        for (int i = 0; i < 5; i++) {
            writer.write(newArrayList(changeset));
        }

        try (Connection connection = graphDatabase().newConnection();
             Statement transaction = connection.createStatement();
             ResultSet changesetSet = transaction.executeQuery("MATCH (changeset:__LiquigraphChangeset) RETURN changeset");
             ResultSet propSet = transaction.executeQuery("MATCH (n:SomeNode) RETURN n.prop AS prop")) {
            assertThat(changesetSet.next()).isTrue();

            Object changesetNode = changesetSet.getObject("changeset");
            assertThat(property(changesetNode, "id")).isEqualTo("identifier");
            assertThat(property(changesetNode, "author")).isEqualTo("fbiville");
            assertThat(property(changesetNode, "checksum"))
                    .isEqualTo(checksum(singletonList(
                            "MERGE (n:SomeNode) " +
                            "ON CREATE SET n.prop = 1 " +
                            "ON MATCH SET n.prop = n.prop + 1")));
            assertThat(changesetSet.next()).as("No more result in changeset result set").isFalse();

            assertThat(propSet.next()).isTrue();
            assertThat(((Number) propSet.getObject("prop")).intValue()).isEqualTo(5);
            assertThat(propSet.next()).as("No more result in test node result set").isFalse();
        }
    }

    @Test
    public void persists_run_on_change_changesets_in_graph_only_once() throws SQLException {
        Changeset changeset = changeset("identifier", "fbiville", "CREATE (n:SomeNode {prop: 1})");
        changeset.setRunOnChange(true);

        writer.write(newArrayList(changeset));

        changeset = changeset(changeset.getId(), changeset.getAuthor(),
                "MERGE (n:SomeNode) " +
                "ON CREATE SET n.prop = 1 " +
                "ON MATCH SET n.prop = n.prop + 1");
        changeset.setRunOnChange(true);

        writer.write(newArrayList(changeset));

        try (Connection connection = graphDatabase().newConnection();
             Statement transaction = connection.createStatement();
             ResultSet changesetSet = transaction.executeQuery("MATCH (changeset:__LiquigraphChangeset) RETURN changeset");
             ResultSet propSet = transaction.executeQuery("MATCH (n:SomeNode) RETURN n.prop AS prop")) {
            assertThat(changesetSet.next()).isTrue();

            Object changesetNode = changesetSet.getObject("changeset");
            assertThat(property(changesetNode, "id")).isEqualTo("identifier");
            assertThat(property(changesetNode, "author")).isEqualTo("fbiville");
            assertThat(property(changesetNode, "checksum"))
                    .isEqualTo(checksum(singletonList(
                            "MERGE (n:SomeNode) " +
                            "ON CREATE SET n.prop = 1 " +
                            "ON MATCH SET n.prop = n.prop + 1")));
            assertThat(changesetSet.next()).as("No more result in changeset result set").isFalse();

            assertThat(propSet.next()).isTrue();
            assertThat(((Number) propSet.getObject("prop")).intValue()).isEqualTo(2);
            assertThat(propSet.next()).as("No more result in test node result set").isFalse();
        }
    }

    @Test
    public void applies_changesets_in_graph_repeatedly_while_postcondition_is_true() throws SQLException {
        try (Connection connection = graphDatabase().newConnection()) {
            given_inserted_data(
                "CREATE (n:SomeNode {prop: 0}), " +
                "       (n)-[:IS_RELATED_TO]->(n2:OtherNode), " +
                "       (n)-[:IS_RELATED_TO]->(n3:OtherNode)",
                connection);

            Changeset changeset = changeset("identifier", "fbiville",
                "MATCH (n:SomeNode)-[r:IS_RELATED_TO]->(n2) " +
                "WITH n, r, n2 " +
                "LIMIT 1 " +
                "DELETE r, n2 " +
                "WITH n " +
                "SET n.prop = n.prop + 1 ");
            changeset.setPostcondition(postcondition(
                "OPTIONAL MATCH (n:SomeNode) " +
                "RETURN EXISTS((n)-->()) AS result"));

            writer.write(newArrayList(changeset));

            try (Statement transaction = connection.createStatement();
                 ResultSet resultSet = transaction.executeQuery(
                     "MATCH (n:SomeNode) " +
                     "RETURN n.prop AS prop")) {

                assertThat(resultSet.next()).isTrue();

                assertThat(resultSet.getLong("prop")).isEqualTo(2);
            }
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

    private static Postcondition postcondition(String query) {
        Postcondition postcondition = new Postcondition();
        SimpleQuery simpleQuery = new SimpleQuery();
        simpleQuery.setQuery(query);
        postcondition.setQuery(simpleQuery);
        return postcondition;
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

    private void assertThatQueryIsExecutedAndHistoryPersisted() throws SQLException {
        try (Connection connection = connectionSupplier.get();
             Statement statement = connection.createStatement();
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

        Object changeset = resultSet.getObject("changeset");
        assertThat(property(changeset, "id")).isEqualTo("identifier");
        assertThat(property(changeset, "author")).isEqualTo("fbiville");
        assertThat(property(changeset, "checksum")).isEqualTo(checksum(singletonList("CREATE (n: SomeNode {text:'yeah'})")));
    }

    private static void assertThatQueryIsExecuted(ResultSet resultSet) throws SQLException {
        Object node = resultSet.getObject("node");
        assertThat(property(node, "text")).isEqualTo("yeah");
    }

    private static void assertThatQueryIsNotExecuted(ResultSet resultSet) throws SQLException {
        assertThat(resultSet.getObject("node")).isNull();
    }

    private static Object property(Object changeset, String name) {
        if (changeset instanceof Map) {
            return ((Map<String,Object>) changeset).get(name);
        }
        assertThat(changeset).isInstanceOf(Node.class);
        return ((Node)changeset).getProperty(name);
    }

    private void given_inserted_data(String query, Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.executeQuery(query);
        }
        connection.commit();
    }

    private class ConnectionSupplier implements Supplier<Connection> {
        @Override
        public Connection get() {
            return graphDatabase().newConnection();
        }
    }
}
