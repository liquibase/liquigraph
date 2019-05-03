/*
 * Copyright 2014-2018 the original author or authors.
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
package org.liquigraph.core.configuration;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Optional;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import static com.google.common.base.Throwables.propagate;

public class ConnectionConfigurationByUri implements ConnectionConfiguration {

    private final String uri;
    private final Optional<String> username;
    private final Optional<String> password;
    private final UriConnectionProvider connectionProvider;

    public ConnectionConfigurationByUri(String uri,
                                        Optional<String> username,
                                        Optional<String> password) {

        this(uri, username, password, DefaultUriConnectionProvider.INSTANCE);
    }

    @VisibleForTesting
    ConnectionConfigurationByUri(String uri,
                                 Optional<String> username,
                                 Optional<String> password,
                                 UriConnectionProvider connectionProvider) {

        this.uri = uri;
        this.username = username;
        this.password = password;
        this.connectionProvider = connectionProvider;
    }

    @Override
    public Connection get() {
        if (username.isPresent()) {
            return connectionProvider.getConnection(uri, username.get(), password.or(""));
        }
        return connectionProvider.getConnection(uri);

    }

    private enum DefaultUriConnectionProvider implements UriConnectionProvider {
        INSTANCE;

        @Override
        public Connection getConnection(String uri) {
            try {
                return DriverManager.getConnection(uri);
            } catch (SQLException e) {
                throw propagate(e);
            }
        }

        @Override
        public Connection getConnection(String uri, String username, String password) {
            try {
                return DriverManager.getConnection(uri, username, password);
            } catch (SQLException e) {
                throw propagate(e);
            }
        }
    }

    @VisibleForTesting
    interface UriConnectionProvider {
        Connection getConnection(String uri);
        Connection getConnection(String uri, String username, String password);
    }
}
