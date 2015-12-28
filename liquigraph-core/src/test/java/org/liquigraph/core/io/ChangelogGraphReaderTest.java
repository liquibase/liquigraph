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

import org.junit.Rule;
import org.junit.Test;
import org.liquigraph.core.EmbeddedGraphDatabaseRule;
import org.liquigraph.core.model.Changeset;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;
import static org.liquigraph.core.model.Checksums.checksum;

public class ChangelogGraphReaderTest {

    @Rule
    public EmbeddedGraphDatabaseRule graph = new EmbeddedGraphDatabaseRule("neotest");

    private ChangelogGraphReader reader = new ChangelogGraphReader();

    @Test
    public void reads_changelog_from_graph_database() throws SQLException {
        try (Connection connection = graph.jdbcConnection()) {

            given_inserted_data(format(
                "CREATE (:__LiquigraphChangelog)<-[:EXECUTED_WITHIN_CHANGELOG {time:1}]-" +
                        "(:__LiquigraphChangeset {" +
                        "   author:'fbiville'," +
                        "   id:'test'," +
                        "   query:%s, " +
                        "   checksum:'%s'" +
                        "})"
                , "'MATCH n RETURN n'", checksum(singletonList("MATCH n RETURN n"))),
                connection
            );

            Collection<Changeset> changesets = reader.read(graph.jdbcConnection());
            assertThat(changesets).extracting("id", "author", "queries", "checksum").containsExactly(
                    tuple("test", "fbiville", singletonList("MATCH n RETURN n"), checksum(singletonList("MATCH n RETURN n")))
            );
            connection.commit();
        }
    }

    @Test
    public void reads_changeset_with_multiple_queries() throws SQLException {
        try (Connection connection = graph.jdbcConnection()) {
            given_inserted_data(format(
               "CREATE (:__LiquigraphChangelog)<-[:EXECUTED_WITHIN_CHANGELOG {time:1}]-" +
                  "(:__LiquigraphChangeset {" +
                  "   author:'fbiville'," +
                  "   id:'test'," +
                  "   query:%s, " +
                  "   checksum:'%s'" +
                  "})",
               "[\"MATCH n RETURN n\", \"MATCH m RETURN m\"]",
               checksum(asList("MATCH n RETURN n", "MATCH m RETURN m"))),
               connection
            );

            Collection<Changeset> changesets = reader.read(graph.jdbcConnection());
            assertThat(changesets).extracting("id", "author", "queries", "checksum").containsExactly(
               tuple(
                  "test",
                  "fbiville",
                  asList("MATCH n RETURN n", "MATCH m RETURN m"),
                  checksum(asList("MATCH n RETURN n", "MATCH m RETURN m"))
               )
            );
            connection.commit();
        }
    }

    private void given_inserted_data(String query, Connection connection) throws SQLException {
        connection.createStatement().executeQuery(query);
    }
}