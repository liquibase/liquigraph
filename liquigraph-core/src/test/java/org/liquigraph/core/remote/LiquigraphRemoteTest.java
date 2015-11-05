package org.liquigraph.core.remote;

import com.google.common.base.Throwables;
import org.liquigraph.core.api.ApiSpecifications;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import static org.junit.Assume.assumeTrue;

public class LiquigraphRemoteTest extends ApiSpecifications {

    private static final String NEO4J_URL = "jdbc:neo4j://localhost:7474/";

    @Override
    protected String getUri() {
        return NEO4J_URL;
    }

    @Override
    protected Connection getJdbcConnection() {
        try {
            return DriverManager.getConnection(String.format(NEO4J_URL + "?user=%s,password=%s", getUsername(), getPassword()));
        } catch (SQLException e) {
            throw Throwables.propagate(e);
        }
    }

    @Override
    protected String getUsername() {
        return "neo4j";
    }

    @Override
    protected String getPassword() {
        return "foobar";
    }

}
