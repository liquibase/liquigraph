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
            Connection connection = DriverManager.getConnection(makeUri(configuration));
            connection.setAutoCommit(false);
            return connection;
        } catch (ClassNotFoundException | SQLException e) {
            throw Throwables.propagate(e);
        }
    }

  private String makeUri(Configuration configuration) {
    Optional<String> username = configuration.username();
    if (!username.isPresent()) {
      return configuration.uri();
    }
    return authenticatedUri(configuration);
  }

  private String authenticatedUri(Configuration configuration) {
    String uri = configuration.uri();
    String firstDelimiter = uri.contains("?") ? "," : "?";
    return uri
      + firstDelimiter + "user=" + configuration.username().get()
      + ",password=" + configuration.password().or("");
  }

}
