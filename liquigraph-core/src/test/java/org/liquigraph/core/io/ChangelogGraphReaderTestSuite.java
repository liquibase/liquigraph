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

import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Iterator;
import org.junit.Test;
import org.liquigraph.core.GraphIntegrationTestSuite;
import org.liquigraph.core.model.Changeset;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.liquigraph.core.model.Checksums.checksum;

abstract class ChangelogGraphReaderTestSuite implements GraphIntegrationTestSuite {

    private ChangelogGraphReader reader = new ChangelogGraphReader();

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

    private void given_inserted_data(String query, Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {
            return;
        }
    }
}
