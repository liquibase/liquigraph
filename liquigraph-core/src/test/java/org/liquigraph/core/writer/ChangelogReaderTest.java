package org.liquigraph.core.writer;

import org.liquigraph.core.model.Changeset;
import org.liquigraph.core.rules.EmbeddedGraphDatabaseRule;
import org.junit.Rule;
import org.junit.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;

import static org.liquigraph.core.model.Checksums.checksum;
import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;

public class ChangelogReaderTest {

    @Rule
    public EmbeddedGraphDatabaseRule graph = new EmbeddedGraphDatabaseRule("neotest");

    private ChangelogReader reader = new ChangelogReader();

    @Test
    public void reads_changelog_from_graph_database() throws SQLException {
        try (Connection connection = graph.jdbcConnection();
             Statement ignored = connection.createStatement()) {

            given_inserted_data(format(
                "CREATE (:__LiquigraphChangelog)<-[:EXECUTED_WITHIN_CHANGELOG {order:1}]-" +
                        "(:__LiquigraphChangeset {" +
                        "   author:'fbiville'," +
                        "   id:'test'," +
                        "   query:'%s', " +
                        "   checksum:'%s'" +
                        "})"
                , "MATCH n RETURN n", checksum("MATCH n RETURN n")),
                connection
            );

            Collection<Changeset> changesets = reader.read(graph.jdbcConnection());
            assertThat(changesets).extracting("id", "author", "query", "checksum").containsExactly(
                    tuple("test", "fbiville", "MATCH n RETURN n", checksum("MATCH n RETURN n"))
            );
            connection.commit();
        }
    }

    private void given_inserted_data(String query, Connection connection) throws SQLException {
        connection.createStatement().executeQuery(query);
    }
}