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
package org.liquigraph.core.io.lock;

import org.junit.Test;
import org.liquigraph.core.exception.LiquigraphLockException;
import org.mockito.InOrder;

import java.sql.Connection;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class LockableConnectionTest {

    @Test
    public void disables_auto_commit() throws Exception {
        Connection delegate = mock(Connection.class);

        LockableConnection.acquire(delegate, mock(LiquigraphLock.class));

        verify(delegate).setAutoCommit(false);
    }

    @Test
    public void restores_auto_commit_upon_close() throws Exception {
        Connection delegate = mock(Connection.class);
        when(delegate.getAutoCommit()).thenReturn(true);

        LockableConnection.acquire(delegate, mock(LiquigraphLock.class)).close();

        InOrder inOrder = inOrder(delegate);
        inOrder.verify(delegate).rollback();
        inOrder.verify(delegate).setAutoCommit(true);
        inOrder.verify(delegate).close(); // cannot set auto-commit after close
    }

    @Test
    public void closes_connection_upon_lock_acquire_error() throws SQLException {
        Connection connection = mock(Connection.class);
        LiquigraphLock lock = mock(LiquigraphLock.class);
        doThrow(LiquigraphLockException.class).when(lock).acquire(any(Connection.class));

        try {
            LockableConnection.acquire(connection, lock);
            fail("Should propagate lock exception");
        }
        catch (RuntimeException ex) {
            assertThat(ex).isInstanceOf(LiquigraphLockException.class);
        }

        verify(connection).close();
    }

    @Test
    public void closes_connection_upon_autocommit_disabling_error() throws SQLException {
        Connection connection = mock(Connection.class);
        doThrow(SQLException.class).when(connection).setAutoCommit(false);
        LiquigraphLock lock = mock(LiquigraphLock.class);

        try {
            LockableConnection.acquire(connection, lock);
            fail("Should propagate autocommit exception");
        }
        catch (RuntimeException ex) {
            assertThat(ex).hasCauseInstanceOf(SQLException.class);
        }

        verify(connection).close();
    }

    @Test
    @SuppressWarnings("unchecked")
    public void add_suppressed_close_exception_connection_upon_acquire_error() throws SQLException {
        Connection connection = mock(Connection.class);
        SQLException sqlException = mock(SQLException.class);
        doThrow(sqlException).when(connection).close();
        LiquigraphLock lock = mock(LiquigraphLock.class);
        doThrow(new LiquigraphLockException("Locking will fail", sqlException)).when(lock).acquire(any(Connection.class));

        try {
            LockableConnection.acquire(connection, lock);
            fail("Should add suppressed close exception");
        }
        catch (RuntimeException ex) {
            assertThat(ex).isInstanceOf(LiquigraphLockException.class);
            assertThat(ex.getSuppressed())
                .containsExactly(sqlException);
        }
    }

}