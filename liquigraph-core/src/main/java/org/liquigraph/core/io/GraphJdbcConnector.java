/**
 * Copyright 2014-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.liquigraph.core.io;

import com.google.common.base.Optional;
import com.google.common.base.Throwables;
import org.liquigraph.core.configuration.Configuration;
import org.liquigraph.core.io.lock.LockableConnection;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class GraphJdbcConnector implements LiquigraphJdbcConnector {

    /**
     * Acquires a new connection to the configured instance
     * and tries to lock it (fail-fast).
     *
     * @see {@link LockableConnection}
     * @param configuration Liquigraph settings
     * @return JDBC connection
     */
    @Override
    public final Connection connect(Configuration configuration) {
        try {
            Class.forName("org.neo4j.jdbc.Driver");
            Connection connection = DriverManager.getConnection(makeUri(configuration));
            connection.setAutoCommit(false);
            return new LockableConnection(connection);
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
