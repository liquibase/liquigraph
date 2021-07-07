/*
 * Copyright 2014-2021 the original author or authors.
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


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Optional;
import java.util.Properties;

import static org.liquigraph.core.exception.Throwables.propagate;

public class ConnectionConfigurationByUri implements ConnectionConfiguration {

    private final String uri;
    private final Properties properties;
    private final UriConnectionSupplier connectionSupplier;

    public ConnectionConfigurationByUri(String uri,
                                        Optional<String> database,
                                        Optional<String> username,
                                        Optional<String> password) {

        this(uri, database, username, password, DefaultUriConnectionSupplier.INSTANCE);
    }

    // visible for testing
    ConnectionConfigurationByUri(String uri,
                                 Optional<String> database,
                                 Optional<String> username,
                                 Optional<String> password,
                                 UriConnectionSupplier connectionSupplier) {

        this.uri = uri;
        this.connectionSupplier = connectionSupplier;
        this.properties = createProperties(database, username, password);
    }

    @Override
    public Connection get() {
        return connectionSupplier.getConnection(uri, properties);
    }


    private static Properties createProperties(Optional<String> database, Optional<String> username, Optional<String> password) {
        Properties props = new Properties();
        username.ifPresent(user -> props.setProperty("user", user));
        password.ifPresent(pw -> props.setProperty("password", pw));
        database.ifPresent(db -> props.setProperty("database", db));
        return props;
    }

    private enum DefaultUriConnectionSupplier implements UriConnectionSupplier {
        INSTANCE;

        @Override
        public Connection getConnection(String uri, Properties properties) {
            try {
                return DriverManager.getConnection(uri, properties);
            } catch (SQLException e) {
                throw propagate(e);
            }
        }

    }

    // visible for testing
    interface UriConnectionSupplier {
        Connection getConnection(String uri, Properties properties);
    }
}
