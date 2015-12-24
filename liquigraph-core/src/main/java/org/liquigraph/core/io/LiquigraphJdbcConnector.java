package org.liquigraph.core.io;

import org.liquigraph.core.configuration.Configuration;

import java.sql.Connection;

public interface LiquigraphJdbcConnector {

    Connection connect(Configuration configuration);
}
