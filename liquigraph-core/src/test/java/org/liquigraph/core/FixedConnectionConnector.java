package org.liquigraph.core;

import org.liquigraph.core.configuration.Configuration;
import org.liquigraph.core.writer.LiquigraphJdbcConnector;

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
