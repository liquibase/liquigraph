/*
 * Copyright 2014-2021 the original author or authors.
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

import java.sql.ResultSet;
import java.util.Collection;
import java.util.Map;

import org.junit.Test;
import org.liquigraph.core.exception.PreconditionNotMetException;
import org.liquigraph.core.model.Changeset;
import org.liquigraph.core.model.Postcondition;
import org.liquigraph.core.model.Precondition;
import org.liquigraph.core.model.PreconditionErrorPolicy;
import org.liquigraph.core.model.SimpleQuery;
import org.liquigraph.testing.JdbcAwareGraphDatabase;
import org.liquigraph.testing.ParameterizedDatabaseIT;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.liquigraph.core.model.Checksums.checksum;

public class ChangelogGraphWriterIT extends ParameterizedDatabaseIT {

    public ChangelogGraphWriterIT(String description, JdbcAwareGraphDatabase graphDb, String uri) {
        super(description, graphDb, uri);
    }

    @Test
    public void persists_changesets_in_graph() {
        graphDb
            .commitNewConnection(uri, connection -> {
                ChangelogGraphWriter writer = new ChangelogGraphWriter(
                graphDb.asConnectionSupplier(uri),
                new ConditionExecutor());

                writer.write(singletonList(changeset("identifier", "fbiville", "CREATE (n: SomeNode {text:'yeah'})")));
            })
            .rollbackNewSingleStatementConnection(uri, statement -> {
                try (ResultSet resultSet = statement.executeQuery(
                     "MATCH  (node: SomeNode), " +
                     "       (changelog:__LiquigraphChangelog)<-[ewc:EXECUTED_WITHIN_CHANGELOG]-(changeset:__LiquigraphChangeset), " +
                     "       (changeset)<-[:EXECUTED_WITHIN_CHANGESET]-(query:__LiquigraphQuery) " +
                     "RETURN ewc.time AS time, changeset, collect(query.query) AS queries, node"
                     )) {

                    assertThat(resultSet.next()).as("Result set should contain 1 row").isTrue();
                    assertThat(resultSet.getLong("time")).isGreaterThan(0);
                    assertThat((Collection<String>) resultSet.getObject("queries")).containsExactly("CREATE (n: SomeNode {text:'yeah'})");
                    Object changeset = resultSet.getObject("changeset");
                    assertThat(property(changeset, "id")).isEqualTo("identifier");
                    assertThat(property(changeset, "author")).isEqualTo("fbiville");
                    assertThat(property(changeset, "checksum")).isEqualTo(checksum(singletonList("CREATE (n: SomeNode {text:'yeah'})")));
                    assertThat(property(resultSet.getObject("node"), "text")).isEqualTo("yeah");
                    assertThat(resultSet.next()).as("No more result in result set").isFalse();
                }
            });
    }

    @Test
    public void persists_changesets_in_graph_when_preconditions_are_met() {
        graphDb
            .commitNewConnection(uri, connection -> {
                ChangelogGraphWriter writer = new ChangelogGraphWriter(
                graphDb.asConnectionSupplier(uri),
                new ConditionExecutor());
                Precondition precondition = precondition(PreconditionErrorPolicy.FAIL, "RETURN true AS result");
                Changeset changeset = changeset("identifier", "fbiville", "CREATE (n: SomeNode {text:'yeah'})", precondition);

                writer.write(singletonList(changeset));
            })
            .rollbackNewSingleStatementConnection(uri, statement -> {
                try (ResultSet resultSet = statement.executeQuery(
                     "MATCH  (node: SomeNode), " +
                     "       (changelog:__LiquigraphChangelog)<-[ewc:EXECUTED_WITHIN_CHANGELOG]-(changeset:__LiquigraphChangeset), " +
                     "       (changeset)<-[:EXECUTED_WITHIN_CHANGESET]-(query:__LiquigraphQuery) " +
                     "RETURN ewc.time AS time, changeset, collect(query.query) AS queries, node"
                     )) {

                    assertThat(resultSet.next()).as("Result set should contain 1 row").isTrue();
                    assertThat(resultSet.getLong("time")).isGreaterThan(0);
                    assertThat((Collection<String>) resultSet.getObject("queries")).containsExactly("CREATE (n: SomeNode {text:'yeah'})");
                    Object changeset = resultSet.getObject("changeset");
                    assertThat(property(changeset, "id")).isEqualTo("identifier");
                    assertThat(property(changeset, "author")).isEqualTo("fbiville");
                    assertThat(property(changeset, "checksum")).isEqualTo(checksum(singletonList("CREATE (n: SomeNode {text:'yeah'})")));
                    assertThat(property(resultSet.getObject("node"), "text")).isEqualTo("yeah");
                    assertThat(resultSet.next()).as("No more result in result set").isFalse();
                }
            });
    }

    @Test
    public void fails_when_precondition_is_not_met_and_configured_to_halt_execution() {
        assertThatThrownBy(
        () -> {
            graphDb.commitNewConnection(uri, connection -> {
                ChangelogGraphWriter writer = new ChangelogGraphWriter(
                graphDb.asConnectionSupplier(uri),
                new ConditionExecutor());

                Precondition precondition = precondition(PreconditionErrorPolicy.FAIL, "RETURN false AS result");
                Changeset changeset = changeset("identifier", "fbiville", "CREATE (n: SomeNode {text:'yeah'})", precondition);

                writer.write(singletonList(changeset));
            });
        })
        .isInstanceOf(PreconditionNotMetException.class)
        .hasMessage("Changeset id=<identifier>, author=<fbiville>: precondition query <RETURN false AS result> failed with policy <FAIL>. Aborting.");
    }

    @Test
    public void skips_changeset_execution_when_precondition_is_not_met_and_configured_to_skip() {
        graphDb
            .commitNewConnection(uri, connection -> {
                ChangelogGraphWriter writer = new ChangelogGraphWriter(
                graphDb.asConnectionSupplier(uri),
                new ConditionExecutor());
                Precondition precondition = precondition(PreconditionErrorPolicy.CONTINUE, "RETURN false AS result");
                Changeset changeset = changeset("identifier", "fbiville", "CREATE (n: SomeNode {text:'yeah'})", precondition);

                writer.write(singletonList(changeset));
            })
            .rollbackNewSingleStatementConnection(uri, statement -> {
                try (ResultSet resultSet = statement.executeQuery(
                     "MATCH (changelog:__LiquigraphChangelog)<-[execution:EXECUTED_WITHIN_CHANGELOG]-(changeset:__LiquigraphChangeset) " +
                     "OPTIONAL MATCH (node :SomeNode) " +
                     "RETURN execution.order AS order, changeset, node")) {

                    assertThat(resultSet.next()).as("No more result in result set").isFalse();
                }
            });
    }

    @Test
    public void persists_changeset_but_does_execute_it_when_precondition_is_not_met_and_configured_to_mark_as_executed() {
        graphDb
            .commitNewConnection(uri, connection -> {
                ChangelogGraphWriter writer = new ChangelogGraphWriter(
                graphDb.asConnectionSupplier(uri),
                new ConditionExecutor());
                Precondition precondition = precondition(PreconditionErrorPolicy.MARK_AS_EXECUTED, "RETURN false AS result");

                writer.write(singletonList(changeset("identifier", "fbiville", "CREATE (n: SomeNode {text:'yeah'})", precondition)));
            })
            .rollbackNewSingleStatementConnection(uri, statement -> {
                try (ResultSet resultSet = statement.executeQuery(
                     "OPTIONAL MATCH  (node: SomeNode) " +
                     "WITH node " +
                     "MATCH  (changelog:__LiquigraphChangelog)<-[ewc:EXECUTED_WITHIN_CHANGELOG]-(changeset:__LiquigraphChangeset), " +
                     "       (changeset)<-[:EXECUTED_WITHIN_CHANGESET]-(query:__LiquigraphQuery) " +
                     "RETURN ewc.time AS time, changeset, collect(query.query) AS queries, node")) {

                    assertThat(resultSet.next()).as("Result set should contain 1 row").isTrue();
                    assertThat(resultSet.getLong("time")).isGreaterThan(0);

                    assertThat((Collection<String>) resultSet.getObject("queries")).containsExactly("CREATE (n: SomeNode {text:'yeah'})");

                    Object changeset = resultSet.getObject("changeset");
                    assertThat(property(changeset, "id")).isEqualTo("identifier");
                    assertThat(property(changeset, "author")).isEqualTo("fbiville");
                    assertThat(property(changeset, "checksum")).isEqualTo(checksum(singletonList("CREATE (n: SomeNode {text:'yeah'})")));
                    assertThat(resultSet.getObject("node")).isNull();
                    assertThat(resultSet.next()).as("No more result in result set").isFalse();
                }
            });
    }

    @Test
    public void executes_changeset_with_multiple_queries() {
        graphDb
            .commitNewConnection(uri, connection -> {
                ChangelogGraphWriter writer = new ChangelogGraphWriter(
                graphDb.asConnectionSupplier(uri),
                new ConditionExecutor());
                Changeset changeset = changeset("id", "fbiville", asList("CREATE (n:Human) RETURN n", "MATCH (n:Human) SET n.age = 42 RETURN n"));

                writer.write(singletonList(changeset));
            })
            .rollbackNewSingleStatementConnection(uri, statement -> {
                try (ResultSet resultSet = statement.executeQuery("MATCH (n:Human) RETURN n.age AS age")) {

                    assertThat(resultSet.next()).isTrue();
                    assertThat(resultSet.getLong("age")).isEqualTo(42);
                    assertThat(resultSet.next()).as("No more result in result set").isFalse();
                }
            })
            .rollbackNewSingleStatementConnection(uri, statement -> {
                try (ResultSet resultSet = statement.executeQuery("MATCH (queries:__LiquigraphQuery) RETURN collect(queries.query) AS query")) {

                    assertThat(resultSet.next()).isTrue();
                    assertThat((Collection<String>) resultSet.getObject("query")).containsOnly(
                    "CREATE (n:Human) RETURN n", "MATCH (n:Human) SET n.age = 42 RETURN n"
                    );
                    assertThat(resultSet.next()).as("No more result in result set").isFalse();
                }
            });
    }

    @Test
    public void persists_run_always_changesets_in_graph_only_once() {
        graphDb
            .commitNewConnection(uri, connection -> {
                ChangelogGraphWriter writer = new ChangelogGraphWriter(
                graphDb.asConnectionSupplier(uri),
                new ConditionExecutor());
                Changeset changeset = changeset("identifier", "fbiville",
                "MERGE (n:SomeNode) " +
                "ON CREATE SET n.prop = 1 " +
                "ON MATCH SET n.prop = n.prop + 1");
                changeset.setRunAlways(true);

                for (int i = 0; i < 5; i++) {
                    writer.write(singletonList(changeset));
                }
            })
            .rollbackNewSingleStatementConnection(uri, statement -> {
                try (ResultSet changesetSet = statement.executeQuery("MATCH (changeset:__LiquigraphChangeset) RETURN changeset");
                     ResultSet propSet = statement.executeQuery("MATCH (n:SomeNode) RETURN n.prop AS prop")) {

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
            });
    }

    @Test
    public void persists_run_on_change_changesets_in_graph_only_once() {
        graphDb
            .commitNewConnection(uri, connection -> {
                ChangelogGraphWriter writer = new ChangelogGraphWriter(
                graphDb.asConnectionSupplier(uri),
                new ConditionExecutor());

                Changeset changeset = changeset("identifier", "fbiville", "CREATE (n:SomeNode {prop: 1})");
                changeset.setRunOnChange(true);

                writer.write(singletonList(changeset));

                changeset = changeset(changeset.getId(), changeset.getAuthor(),
                "MERGE (n:SomeNode) " +
                "ON CREATE SET n.prop = 1 " +
                "ON MATCH SET n.prop = n.prop + 1");
                changeset.setRunOnChange(true);

                writer.write(singletonList(changeset));
            })
            .rollbackNewSingleStatementConnection(uri, statement -> {
                try (ResultSet changesetSet = statement.executeQuery("MATCH (changeset:__LiquigraphChangeset) RETURN changeset");
                     ResultSet propSet = statement.executeQuery("MATCH (n:SomeNode) RETURN n.prop AS prop")) {

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
            });
    }

    @Test
    public void applies_changesets_in_graph_repeatedly_while_postcondition_is_true() {
        graphDb
            .commitNewSingleStatementConnection(uri, statement -> {
                statement.execute("CREATE (n:SomeNode {prop: 0}), " +
                    "       (n)-[:IS_RELATED_TO]->(n2:OtherNode), " +
                    "       (n)-[:IS_RELATED_TO]->(n3:OtherNode)");
            })
            .commitNewConnection(uri, connection -> {
                ChangelogGraphWriter writer = new ChangelogGraphWriter(
                graphDb.asConnectionSupplier(uri), new ConditionExecutor());
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

                writer.write(singletonList(changeset));
            })
            .rollbackNewSingleStatementConnection(uri, statement -> {
                try (ResultSet resultSet = statement.executeQuery(
                     "MATCH (n:SomeNode) " +
                     "RETURN n.prop AS prop")) {

                    assertThat(resultSet.next()).isTrue();
                    assertThat(resultSet.getLong("prop")).isEqualTo(2);
                }
            });
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

    private static Object property(Object changeset, String name) {
        return ((Map<String, Object>) changeset).get(name);
    }
}
