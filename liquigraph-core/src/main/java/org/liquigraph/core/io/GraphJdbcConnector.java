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
package org.liquigraph.core.io;

import org.liquigraph.core.configuration.Configuration;
import org.liquigraph.core.io.lock.LiquigraphLock;
import org.liquigraph.core.io.lock.LockableConnection;

import java.sql.Connection;
import java.util.function.Supplier;

public class GraphJdbcConnector {

    private final LiquigraphLock lock;
    private final Configuration configuration;

    public GraphJdbcConnector(Configuration configuration) {
        lock = new LiquigraphLock(configuration.dataSourceConfiguration());
        this.configuration = configuration;
    }

    /**
     * Acquires a new connection to the configured instance
     * and tries to lock it (fail-fast).
     *
     * @see LockableConnection
     * @return JDBC connection
     */
    public final Connection connect() {
        Connection connection = configuration.dataSourceConfiguration().get();
        return LockableConnection.acquire(connection, lock);
    }

}
