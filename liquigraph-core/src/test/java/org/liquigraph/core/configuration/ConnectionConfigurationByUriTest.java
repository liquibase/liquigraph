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

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Supplier;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.sql.Connection;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.rules.ExpectedException.none;
import static org.mockito.Mockito.mock;

public class ConnectionConfigurationByUriTest {

    @Rule
    public ExpectedException thrown = none();

    @Test
    public void connects_via_the_provided_uri() {
        Connection expectedConnection = mock(Connection.class);
        Supplier<Connection> connectionProvider = new ConnectionConfigurationByUri(
                "jdbc:neo4j:mem:mydb",
                Optional.<String>absent(),
                Optional.<String>absent(),
                new MockedConnectionFunction(expectedConnection)
        );

        Connection connection = connectionProvider.get();

        assertThat(connection).isSameAs(expectedConnection);
    }

    @Test
    public void propagates_any_datasource_access_exceptions() {
        RuntimeException failure = new RuntimeException("oopsie");
        Supplier<Connection> connectionProvider = new ConnectionConfigurationByUri(
            "jdbc:neo4j:mem:mydb",
            Optional.<String>absent(),
            Optional.<String>absent(),
            new ThrowingConnectionFunction(failure)
        );

        thrown.expect(sameInstance(failure));

        connectionProvider.get();

    }

    private static class ThrowingConnectionFunction implements Function<String, Connection> {
        private final RuntimeException exception;

        public ThrowingConnectionFunction(RuntimeException exception) {
            this.exception = exception;
        }

        @Override
        public Connection apply(String s) {
            throw exception;
        }
    }

    private static class MockedConnectionFunction implements Function<String, Connection> {
        private final Connection connection;

        public MockedConnectionFunction(Connection connection) {
            this.connection = connection;
        }

        @Override
        public Connection apply(String ignored) {
            return connection;
        }
    }
}