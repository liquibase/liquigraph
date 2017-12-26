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
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Throwables;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import static com.google.common.base.Throwables.propagate;

public class ConnectionConfigurationByUri implements ConnectionConfiguration {

    private final String uri;
    private final Optional<String> username;
    private final Optional<String> password;
    private final Function<String, Connection> driverManager;

    public ConnectionConfigurationByUri(String uri,
                                        Optional<String> username,
                                        Optional<String> password) {

        this(uri, username, password, UriToConnectionFunction.INSTANCE);
    }

    @VisibleForTesting
    ConnectionConfigurationByUri(String uri,
                                 Optional<String> username,
                                 Optional<String> password,
                                 Function<String, Connection> driverManager) {

        this.uri = uri;
        this.username = username;
        this.password = password;
        this.driverManager = driverManager;
    }

    @Override
    public Connection get() {
        return driverManager.apply(uri());
    }

    private String uri() {
        if (!username.isPresent()) {
            return uri;
        }
        return authenticatedUri();
    }

    private String authenticatedUri() {
        String firstDelimiter = uri.contains("?") ? "," : "?";
        return uri
                + firstDelimiter + "user=" + username.get()
                + ",password=" + password.or("");
    }

    private enum UriToConnectionFunction implements Function<String, Connection> {
        INSTANCE;

        @Override
        public Connection apply(String uri) {
            try {
                return DriverManager.getConnection(uri);
            } catch (SQLException e) {
                throw propagate(e);
            }
        }
    }
}
