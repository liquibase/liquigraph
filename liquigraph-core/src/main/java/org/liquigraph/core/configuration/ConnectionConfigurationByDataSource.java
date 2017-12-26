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

import com.google.common.base.Optional;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

import static com.google.common.base.Throwables.propagate;

public class ConnectionConfigurationByDataSource implements ConnectionConfiguration {

    private final DataSource dataSource;
    private final Optional<String> username;
    private final Optional<String> password;

    public ConnectionConfigurationByDataSource(DataSource dataSource,
                                               Optional<String> username,
                                               Optional<String> password) {
        this.dataSource = dataSource;
        this.username = username;
        this.password = password;
    }

    @Override
    public Connection get() {
        try {
            return connection();
        } catch (SQLException e) {
            throw propagate(e);
        }
    }

    private Connection connection() throws SQLException {
        if (!username.isPresent()) {
            return dataSource.getConnection();
        }
        return dataSource.getConnection(username.get(), password.or(""));
    }
}
