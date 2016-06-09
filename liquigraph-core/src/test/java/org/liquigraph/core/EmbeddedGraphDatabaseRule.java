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
package org.liquigraph.core;

import com.google.common.base.Optional;
import org.junit.rules.ExternalResource;
import org.neo4j.harness.ServerControls;
import org.neo4j.harness.TestServerBuilder;
import org.neo4j.harness.TestServerBuilders;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import static com.google.common.base.Optional.absent;
import static com.google.common.base.Throwables.propagate;

public class EmbeddedGraphDatabaseRule extends ExternalResource
                                       implements GraphDatabaseRule {

    private final TestServerBuilder builder;
    private ServerControls controls;
    private Connection connection;

    public EmbeddedGraphDatabaseRule() {
        builder = TestServerBuilders.newInProcessBuilder();
    }

    @Override
    public Connection connection() {
        return connection;
    }

    @Override
    public String uri() {
        return "jdbc:neo4j:" + controls.boltURI() + "?noSsl";
    }

    @Override
    public Optional<String> username() {
        return absent();
    }

    @Override
    public Optional<String> password() {
        return absent();
    }

    protected void before() {
        try {
            controls = builder.newServer();
            connection = DriverManager.getConnection(uri());
            connection.setAutoCommit(false);
        } catch (SQLException e) {
            throw propagate(e);
        }
    }

    protected void after() {
        try {
            if (!connection.isClosed()) {
                connection.close();
            }
            controls.close();
        } catch (SQLException e) {
            throw propagate(e);
        }
    }

}
