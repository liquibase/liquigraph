package org.liquigraph.core.api;

import org.junit.Rule;
import org.liquigraph.core.rules.EmbeddedGraphDatabaseRule;

import java.sql.Connection;

public class LiquigraphLocalTest extends ApiSpecifications {

    @Rule
    public EmbeddedGraphDatabaseRule graph = new EmbeddedGraphDatabaseRule("neo");

    @Override
    protected String getUri() {
        return graph.uri();
    }

    @Override
    protected Connection getJdbcConnection() {
        return graph.jdbcConnection();
    }

    @Override
    protected String getUsername() {
        return null;
    }

    @Override
    protected String getPassword() {
        return null;
    }
}
