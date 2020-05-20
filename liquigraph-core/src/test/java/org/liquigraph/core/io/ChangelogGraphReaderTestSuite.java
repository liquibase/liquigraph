/*
 * Copyright 2014-2020 the original author or authors.
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

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import org.junit.Test;
import org.liquigraph.core.GraphIntegrationTestSuite;
import org.liquigraph.core.model.Changeset;
import org.slf4j.bridge.SLF4JBridgeHandler;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.liquigraph.core.model.Checksums.checksum;

abstract class ChangelogGraphReaderTestSuite implements GraphIntegrationTestSuite {

    static {
        SLF4JBridgeHandler.removeHandlersForRootLogger();
    }

    private ChangelogGraphReader reader = new ChangelogGraphReader(emptyList());

    @Test
    public void reads_changelog_from_graph_database() throws SQLException {
        try (Connection connection = graphDatabase().newConnection()) {
            String query = "MATCH n RETURN n";
            given_inserted_data(format(
                "CREATE (:__LiquigraphChangelog)<-[:EXECUTED_WITHIN_CHANGELOG {time:1}]-" +
                    "(:__LiquigraphChangeset {" +
                    "   author:'fbiville'," +
                    "   id:'test'," +
                    "   checksum:'%s'" +
                    "})<-[:EXECUTED_WITHIN_CHANGESET]-(:__LiquigraphQuery {query: '%s'})"
                , checksum(singletonList(query)), query),
                connection
            );

            Collection<Changeset> changesets = reader.read(connection);

            assertThat(changesets).hasSize(1);
            Changeset changeset = changesets.iterator().next();
            assertThat(changeset.getId()).isEqualTo("test");
            assertThat(changeset.getChecksum()).isEqualTo(checksum(singletonList(query)));
            assertThat(changeset.getAuthor()).isEqualTo("fbiville");
            assertThat(changeset.getQueries()).containsExactly(query);
        }
    }

    @Test
    public void reads_changeset_with_multiple_queries() throws SQLException {
        try (Connection connection = graphDatabase().newConnection()) {
            given_inserted_data(format(
                "CREATE     (:__LiquigraphChangelog)<-[:EXECUTED_WITHIN_CHANGELOG {time:1}]-" +
                    "           (changeset:__LiquigraphChangeset {" +
                    "               author:'fbiville'," +
                    "               id:'test'," +
                    "               checksum:'%s'" +
                    "           }), " +
                    "           (changeset)<-[:EXECUTED_WITHIN_CHANGESET {order: 1} ]-(:__LiquigraphQuery {query: '%s'}), " +
                    "           (changeset)<-[:EXECUTED_WITHIN_CHANGESET {order: 0} ]-(:__LiquigraphQuery {query: '%s'}), " +
                    "           (changeset)<-[:EXECUTED_WITHIN_CHANGESET {order: 2}]-(:__LiquigraphQuery {query: '%s'})",
                checksum(asList("MATCH m RETURN m", "MATCH n RETURN n", "Match o Return o")),
                "MATCH n RETURN n",
                "MATCH m RETURN m",
                "MATCH o RETURN o"),
                connection
            );

            Collection<Changeset> changesets = reader.read(connection);

            assertThat(changesets).hasSize(1);
            Changeset changeset = changesets.iterator().next();
            assertThat(changeset.getId()).isEqualTo("test");
            assertThat(changeset.getChecksum()).isEqualTo(checksum(asList("MATCH m RETURN m", "MATCH n RETURN n", "Match o Return o")));
            assertThat(changeset.getAuthor()).isEqualTo("fbiville");
            assertThat(changeset.getQueries()).containsExactly("MATCH m RETURN m", "MATCH n RETURN n", "MATCH o RETURN o");
        }
    }

    @Test
    public void migrates_pre_1_0_rc3_changelog_before_reading() throws SQLException {
        try (Connection connection = graphDatabase().newConnection()) {
            String[] queries = { "MATCH m RETURN m", "MATCH n RETURN n", "Match o Return o" };
            given_inserted_data(format(
                    "CREATE     (changelog:__LiquigraphChangelog)<-[:EXECUTED_WITHIN_CHANGELOG {order: 1}]-" +
                        "           (:__LiquigraphChangeset {" +
                        "               author:'fbiville'," +
                        "               id:'test1'," +
                        "               checksum:'%s'," +
                        "               query:'%s'" +
                        "           }), " +
                        "           (changelog)<-[:EXECUTED_WITHIN_CHANGELOG {order: 0}]-" +
                        "           (:__LiquigraphChangeset {" +
                        "               author:'fbiville'," +
                        "               id:'test0'," +
                        "               checksum:'%s'," +
                        "               query:'%s'" +
                        "           }), " +
                        "           (changelog)<-[:EXECUTED_WITHIN_CHANGELOG {order: 2}]-" +
                        "           (:__LiquigraphChangeset {" +
                        "               author:'fbiville'," +
                        "               id:'test2'," +
                        "               checksum:'%s'," +
                        "               query:'%s'" +
                        "           })",
                    checksum(singletonList(queries[1])),
                    queries[1],
                    checksum(singletonList(queries[0])),
                    queries[0],
                    checksum(singletonList(queries[2])),
                    queries[2]),
                    connection
            );

            Iterator<Changeset> changesets = reader.read(connection).iterator();

            Changeset changeset = changesets.next();
            assertThat(changeset.getId()).isEqualTo("test0");
            assertThat(changeset.getAuthor()).isEqualTo("fbiville");
            assertThat(changeset.getChecksum()).isEqualTo(checksum(singletonList(queries[0])));
            assertThat(changeset.getQueries()).containsExactly(queries[0]);
            changeset = changesets.next();
            assertThat(changeset.getId()).isEqualTo("test1");
            assertThat(changeset.getAuthor()).isEqualTo("fbiville");
            assertThat(changeset.getChecksum()).isEqualTo(checksum(singletonList(queries[1])));
            assertThat(changeset.getQueries()).containsExactly(queries[1]);
            changeset = changesets.next();
            assertThat(changeset.getId()).isEqualTo("test2");
            assertThat(changeset.getAuthor()).isEqualTo("fbiville");
            assertThat(changeset.getChecksum()).isEqualTo(checksum(singletonList(queries[2])));
            assertThat(changeset.getQueries()).containsExactly(queries[2]);
            assertThat(changesets.hasNext()).as("Result iterator is exhausted").isFalse();
        }
    }

    @Test
    public void persisted_changesets_have_empty_checksum_then_verify_db_updated() throws SQLException {
        List<Changeset> declaredChangesets = new ArrayList<>();
        declaredChangesets.add(changeset("identifier", "fbiville", "CREATE (n: SomeNode {text:'yeah'})"));
        declaredChangesets.add(changeset("identifier2", "fbiville2", "CREATE (n: SomeNode {text:'yeah2'})"));
        reader = new ChangelogGraphReader(declaredChangesets);
        try (Connection connection = graphDatabase().newConnection()) {
            Changeset missingChecksumChangeset = changeset("identifier", "fbiville", "CREATE (n: SomeNode {text:'yeah'})", null);
            List<Changeset> persistedChangesets = asList(
                missingChecksumChangeset,
                changeset("identifier2", "fbiville2", "CREATE (n: SomeNode {text:'yeah2'})")
            );
            create(persistedChangesets, connection);

            Collection<Changeset> result = reader.read(connection);

            verifyChecksumInDatabase(declaredChangesets.get(0), connection);
            assertThat(result).filteredOn(it -> it.equals(missingChecksumChangeset))
                .extracting("checksum")
                .isNotNull();
        }
    }

    @Test
    public void persisted_changesets_have_empty_checksum_but_do_not_match_declared_changesets_then_verify_db_not_updated() throws SQLException {

        List<Changeset> persistedChangesets = singletonList(
            changeset("identifier", "fbiville", "CREATE (n: SomeNode {text:'yeah'})", null)
        );
        try (Connection connection = graphDatabase().newConnection()) {
            create(persistedChangesets, connection);

            Collection<Changeset> result = reader.read(connection);
            verifyChecksumInDatabase(persistedChangesets.get(0), connection);
        }
    }

    private void verifyChecksumInDatabase(Changeset changeset, Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(format(
                 "MATCH (c:__LiquigraphChangeset {id:'%s'})" +
                 "RETURN c.checksum"
                 , changeset.getId()
             ))) {

            assertThat(resultSet.next()).isTrue();
            assertThat(resultSet.getString(1)).isEqualTo(changeset.getChecksum());
        }
    }

    private void create(List<Changeset> changesetsToPersist, Connection connection) throws SQLException {
        for (Changeset changeset : changesetsToPersist) {
            if (changeset.getQueries().size() != 1) {
                throw new IllegalArgumentException("Changesets with multiple queries are not implemented in this test");
            }
            String query = changeset.getQueries().iterator().next();
            if (changeset.getChecksum() != null) {
                given_inserted_data(
                    "CREATE (:__LiquigraphChangelog)<-[:EXECUTED_WITHIN_CHANGELOG {time:1}]-" +
                    "(:__LiquigraphChangeset {" +
                    "   author: ?," +
                    "   id: ?," +
                    "   checksum: ?" +
                    "})<-[:EXECUTED_WITHIN_CHANGESET]-(:__LiquigraphQuery {query: ?})",
                    asList(changeset.getAuthor(), changeset.getId(), changeset.getChecksum(), query),
                    connection
                );
            } else {
                given_inserted_data(
                    "CREATE (:__LiquigraphChangelog)<-[:EXECUTED_WITHIN_CHANGELOG {time:1}]-" +
                    "(:__LiquigraphChangeset {" +
                    "   author: ?," +
                    "   id:?" +
                    "})<-[:EXECUTED_WITHIN_CHANGESET]-(:__LiquigraphQuery {query: ?})",
                    asList(changeset.getAuthor(), changeset.getId(), query),
                    connection
                );
            }
        }
    }

    private void given_inserted_data(String query, Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {
            return;
        }
    }

    private void given_inserted_data(String query, List<Object> params, Connection connection) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            for (int i=0; i< params.size(); i++) {
                statement.setObject(i + 1, params.get(i));
            }
            statement.executeUpdate();
        }
    }

    private Changeset changeset(String identifier, String author, String query) {
        Collection<String> queries = singletonList(query);
        return changeset(identifier, author, queries);
    }

    private Changeset changeset(String identifier, String author, String query, String checksum) {
        Collection<String> queries = singletonList(query);
        Changeset changeset = changeset(identifier, author, queries);
        changeset.setChecksum(checksum);
        return changeset;
    }

    private Changeset changeset(String identifier, String author, Collection<String> queries) {
        Changeset changeset = new Changeset();
        changeset.setId(identifier);
        changeset.setAuthor(author);
        changeset.setQueries(queries);
        return changeset;
    }
}
