package org.liquigraph.core.api;

import org.liquigraph.core.configuration.ConfigurationBuilder;
import org.liquigraph.core.rules.EmbeddedGraphDatabaseRule;
import org.junit.Rule;
import org.junit.Test;

import java.sql.*;

import static org.assertj.core.api.Assertions.assertThat;

public class LiquigraphTest {

    @Rule
    public EmbeddedGraphDatabaseRule graph = new EmbeddedGraphDatabaseRule("neo");

    private Liquigraph liquigraph = new Liquigraph();

    @Test
    public void runs_migrations_against_embedded_graph_with_failed_precondition_whose_changeset_is_marked_as_executed() throws SQLException {
        liquigraph.runMigrations(
                new ConfigurationBuilder()
                        .withRunMode()
                        .withMasterChangelogLocation("changelog/changelog-with-1-node.xml")
                        .withUri(graph.uri())
                        .build()
        );

        try (Connection connection = graph.jdbcConnection()) {
            try (Statement statement = connection.createStatement();
                 ResultSet resultSet = statement.executeQuery(
                     "MATCH (human:Human {name: 'fbiville'}) RETURN human"
                 )) {

                assertThat(resultSet.next()).isTrue();
                assertThat(resultSet.next()).isFalse();
                connection.commit();
            }

            try (PreparedStatement statement = connection.prepareStatement(
                "MATCH (changeset:__LiquigraphChangeset {id: {1}}) RETURN changeset"
            )) {

                statement.setObject(1, "insert-fbiville");
                ResultSet resultSet = statement.executeQuery();
                assertThat(resultSet.next()).isTrue();
                assertThat(resultSet.next()).isFalse();

                statement.setObject(1, "insert-fbiville-again");
                resultSet = statement.executeQuery();
                assertThat(resultSet.next()).isTrue();
                assertThat(resultSet.next()).isFalse();
                connection.commit();
            }
        }
    }

}