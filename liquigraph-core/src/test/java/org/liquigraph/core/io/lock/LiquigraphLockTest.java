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
package org.liquigraph.core.io.lock;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.function.Supplier;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.liquigraph.core.exception.LiquigraphLockException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.matches;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class LiquigraphLockTest {

    @InjectMocks
    private LiquigraphLock lock;
    @Mock(answer = RETURNS_DEEP_STUBS)
    private Connection connection;
    @Mock
    private PreparedStatement createStatement;
    @Mock
    private PreparedStatement deleteStatement;
    @Mock
    private Supplier<Connection> connectionSupplier;

    @Before
    public void setUpConnection() throws SQLException {
        when(connection.prepareStatement(matches("CREATE\\s+\\(\\w*:__LiquigraphLock[^)]*\\).*"))).thenReturn(createStatement);
        when(connection.prepareStatement(matches("MATCH\\s+\\((\\w+):__LiquigraphLock[^)]*\\)\\s+DELETE\\s+\\1"))).thenReturn(deleteStatement);
        when(connectionSupplier.get()).thenReturn(connection);
    }

    @After
    public void tearDown() {
        lock.cleanup();
    }

    @Test
    public void should_create_the_lock() throws SQLException {
        lock.acquire(connection);

        verify(createStatement).execute();
        verify(connection, times(3)).commit();
    }

    @Test
    public void should_create_the_lock_once() throws SQLException {
        lock.acquire(connection);
        Connection connection2 = mock(Connection.class, RETURNS_DEEP_STUBS);

        lock.acquire(connection2);

        verify(createStatement).execute();
        verify(connection, times(3)).commit();
        verifyNoInteractions(connection2);
    }

    @Test
    public void should_fail_when_the_lock_cannot_be_created() throws SQLException {
        when(createStatement.execute()).thenThrow(SQLException.class);

        assertThatThrownBy(() -> lock.acquire(connection)).isInstanceOf(LiquigraphLockException.class);
    }

    @Test
    public void should_fail_when_the_lock_constraint_existence_fails() throws SQLException {
        Statement constraintStatement = mock(Statement.class);
        when(connection.createStatement()).thenReturn(constraintStatement);
        when(constraintStatement.executeQuery(anyString())).thenThrow(SQLException.class);

        assertThatThrownBy(() -> lock.acquire(connection)).isInstanceOf(LiquigraphLockException.class);
    }

    @Test
    public void should_fail_when_the_lock_cannot_be_constrained() throws SQLException {
        Statement constraintStatement = mock(Statement.class);
        when(connection.createStatement()).thenReturn(constraintStatement);
        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getBoolean("result")).thenReturn(false);
        when(constraintStatement.executeQuery(anyString())).thenReturn(resultSet);
        when(constraintStatement.execute(anyString())).thenThrow(SQLException.class);

        assertThatThrownBy(() -> lock.acquire(connection)).isInstanceOf(LiquigraphLockException.class);
    }

    @Test
    public void should_delete_the_lock() throws SQLException {
        lock.acquire(connection);

        lock.release(connection);

        verify(deleteStatement).execute();
        verify(connection, times(4)).commit();
    }

    @Test
    public void should_delete_the_lock_once() throws SQLException {
        lock.acquire(connection);
        lock.release(connection);

        lock.release(connection);

        verify(deleteStatement).execute();
        verify(connection, times(4)).commit();
    }

    @Test
    public void should_not_fail_when_the_lock_cannot_be_deleted() throws SQLException {
        when(deleteStatement.execute()).thenThrow(SQLException.class);
        lock.acquire(connection);

        lock.release(connection);
        verify(connection, times(3)).commit();
    }

    @Test
    public void should_clean_up_an_unreleased_connection_and_delete_the_lock() throws SQLException {
        lock.acquire(connection);

        lock.cleanup();

        verify(deleteStatement).execute();
        verify(connection, times(4)).commit();
    }
}
