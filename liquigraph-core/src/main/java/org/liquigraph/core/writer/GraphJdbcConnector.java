package org.liquigraph.core.writer;

import com.google.common.base.Optional;
import com.google.common.base.Throwables;
import org.liquigraph.core.configuration.Configuration;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class GraphJdbcConnector {

    public final Connection connect(Configuration configuration) {
        try {
            Class.forName("org.neo4j.jdbc.Driver");
            Connection connection = DriverManager.getConnection(configuration.uri());
            Optional<String> username = configuration.username();
            if (username.isPresent()) {
                connection.setClientInfo("user", username.get());
                connection.setClientInfo("password", configuration.password().get());
            }
            connection.setAutoCommit(false);
            return connection;
        } catch (ClassNotFoundException | SQLException e) {
            throw Throwables.propagate(e);
        }
    }

}
