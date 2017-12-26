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
import com.google.common.base.Supplier;
import org.hamcrest.CoreMatchers;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.rules.ExpectedException.none;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


public class ConnectionConfigurationByDataSourceTest {

    @Rule
    public ExpectedException thrown = none();

    @Test
    public void connects_via_the_provided_datasource() throws SQLException {
        DataSource dataSource = mock(DataSource.class);
        Supplier<Connection> connectionProvider = new ConnectionConfigurationByDataSource(
            dataSource,
            Optional.<String>absent(),
            Optional.<String>absent()
        );

        connectionProvider.get();

        verify(dataSource).getConnection();
    }

    @Test
    public void connects_via_the_provided_datasource_with_passwordless_authentication() throws SQLException {
        DataSource dataSource = mock(DataSource.class);
        Supplier<Connection> connectionProvider = new ConnectionConfigurationByDataSource(
            dataSource,
            Optional.of("user"),
            Optional.<String>absent()
        );

        connectionProvider.get();

        verify(dataSource).getConnection("user", "");
    }

    @Test
    public void connects_via_the_provided_datasource_with_authentication() throws SQLException {
        DataSource dataSource = mock(DataSource.class);
        Supplier<Connection> connectionProvider = new ConnectionConfigurationByDataSource(
            dataSource,
            Optional.of("user"),
            Optional.of("s3cr3t")
        );

        connectionProvider.get();

        verify(dataSource).getConnection("user", "s3cr3t");
    }

    @Test
    public void propagates_any_datasource_access_exceptions() throws SQLException {
        DataSource dataSource = mock(DataSource.class);
        when(dataSource.getConnection()).thenThrow(SQLException.class);
        Supplier<Connection> connectionProvider = new ConnectionConfigurationByDataSource(
                dataSource,
                Optional.<String>absent(),
                Optional.<String>absent()
        );
        thrown.expect(RuntimeException.class);
        thrown.expectCause(CoreMatchers.isA(SQLException.class));

        connectionProvider.get();
    }

    @Test
    public void propagates_any_datasource_access_exceptions_with_authentication() throws SQLException {
        DataSource dataSource = mock(DataSource.class);
        when(dataSource.getConnection("user", "s3cr3t")).thenThrow(SQLException.class);
        Supplier<Connection> connectionProvider = new ConnectionConfigurationByDataSource(
                dataSource,
                Optional.of("user"),
                Optional.of("s3cr3t")
        );
        thrown.expect(RuntimeException.class);
        thrown.expectCause(CoreMatchers.isA(SQLException.class));


        connectionProvider.get();
    }
}