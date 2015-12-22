package org.liquigraph.core.writer;

import org.liquigraph.core.configuration.Configuration;

import java.sql.Connection;

public interface LiquigraphJdbcConnector {

    Connection connect(Configuration configuration);
}
