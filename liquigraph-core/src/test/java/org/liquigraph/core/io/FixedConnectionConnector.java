package org.liquigraph.core.io;

import org.liquigraph.core.configuration.Configuration;

import java.sql.Connection;

public class FixedConnectionConnector implements LiquigraphJdbcConnector {

    private final Connection connection;

    public FixedConnectionConnector(Connection connection) {
        this.connection = connection;
    }

    @Override
    public Connection connect(Configuration configuration) {
        return connection;
    }
}
