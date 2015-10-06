package org.liquigraph.core.api;

import org.junit.Test;
import org.liquigraph.core.configuration.Configuration;
import org.liquigraph.core.configuration.ConfigurationBuilder;

import java.sql.*;

import static org.assertj.core.api.Assertions.assertThat;

public abstract class ApiSpecifications {

    private Liquigraph liquigraph = new Liquigraph();

    @Test
    public void runs_migrations_against_embedded_graph_with_failed_precondition_whose_changeset_is_marked_as_executed() throws SQLException {
        liquigraph.runMigrations(configuration("changelog/changelog-with-1-node.xml"));

        try (Connection connection = getJdbcConnection()) {
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

    @Test
    public void runs_migrations_with_schema_changes() throws SQLException {
        liquigraph.runMigrations(configuration("schema/schema-changelog.xml"));

        try (Connection connection = getJdbcConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("MATCH (foo:Foo {bar: 123}) RETURN foo")) {

            assertThat(resultSet.next()).isTrue();
            assertThat(resultSet.next()).isFalse();
            connection.commit();
        }
    }

    @Test
    public void runs_migrations_with_schema_changes_and_preconditions() throws SQLException {
        liquigraph.runMigrations(configuration("schema/schema-preconditions-changelog.xml"));

        try (Connection connection = getJdbcConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("MATCH (foo:Foo {bar: 123}) RETURN foo")) {

            assertThat(resultSet.next()).isTrue();
            assertThat(resultSet.next()).isFalse();
            connection.commit();
        }
    }

    private Configuration configuration(String changelog) {
        return new ConfigurationBuilder()
            .withRunMode()
            .withMasterChangelogLocation(changelog)
            .withUri(getUri())
            .withUsername(getUsername())
            .withPassword(getPassword())
            .build();
    }

    protected abstract String getUri();

    protected abstract Connection getJdbcConnection();

    protected abstract String getUsername();

    protected abstract String getPassword();

}