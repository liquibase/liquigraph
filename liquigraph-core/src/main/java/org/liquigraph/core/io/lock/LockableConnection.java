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

import java.sql.Array;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.NClob;
import java.sql.PreparedStatement;
import java.sql.SQLClientInfoException;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Savepoint;
import java.sql.Statement;
import java.sql.Struct;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executor;

import static com.google.common.base.Throwables.propagate;

/**
 * This JDBC connection decorator writes a (:__LiquigraphLock)
 * Neo4j node in order to prevent concurrent executions.
 *
 * Closing this connection will delete the created "Lock" node.
 *
 * A shutdown hook is executed to remove the lock node if and
 * only if the connection has not been properly closed.
 *
 * Please note that any {@link Connection} passed to this decorator
 * will be set to auto-commit: false`. The auto-commit property
 * is restored on close. The latter is done in order to minimize
 * side effects when connections are recycled by a connection pool,
 * for instance.
 */
public final class LockableConnection implements Connection {
    private final Connection delegate;
    private final LiquigraphLock lock;
    private final boolean previousAutoCommit;

    private LockableConnection(Connection delegate, boolean previousAutoCommit, LiquigraphLock lock) {
        this.delegate = delegate;
        this.previousAutoCommit = previousAutoCommit;
        this.lock = lock;
    }

    public static LockableConnection acquire(Connection delegate, LiquigraphLock lock) {
        LockableConnection connection = null;
        try {
            boolean previousAutoCommit = delegate.getAutoCommit();
            delegate.setAutoCommit(false);

            connection = new LockableConnection(delegate, previousAutoCommit, lock);
            lock.acquire(connection);
            return connection;
        } catch (SQLException | RuntimeException e) {
            try {
                if (connection != null) {
                    connection.close();
                }
                else {
                    delegate.close();
                }
            } catch (SQLException exception) {
                e.addSuppressed(exception);
            }
            throw propagate(e);
        }
    }

    /**
     * Removes lock node and removes the related cleanup
     * task shutdown hook before closing the underlying
     * connection.
     *
     * Pending transactions are explicitly rolled back
     * before resetting auto-commit. They could otherwise
     * end up being committed in delegate#close()
     * if auto-commit was reset to true.
     *
     * @see ShutdownTask
     */
    @Override
    public void close() throws SQLException {
        lock.release(this);
        rollback();
        delegate.setAutoCommit(previousAutoCommit);
        delegate.close();
    }

    public Statement createStatement() throws SQLException {
        return delegate.createStatement();
    }

    public void commit() throws SQLException {
        delegate.commit();
    }

    public NClob createNClob() throws SQLException {
        return delegate.createNClob();
    }

    public Map<String, Class<?>> getTypeMap() throws SQLException {
        return delegate.getTypeMap();
    }

    public void setHoldability(int holdability) throws SQLException {
        delegate.setHoldability(holdability);
    }

    public void releaseSavepoint(Savepoint savepoint) throws SQLException {
        delegate.releaseSavepoint(savepoint);
    }

    public Array createArrayOf(String typeName, Object[] elements) throws SQLException {
        return delegate.createArrayOf(typeName, elements);
    }

    public void rollback(Savepoint savepoint) throws SQLException {
        delegate.rollback(savepoint);
    }

    public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
        return delegate.prepareStatement(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
    }

    public Struct createStruct(String typeName, Object[] attributes) throws SQLException {
        return delegate.createStruct(typeName, attributes);
    }

    public void setAutoCommit(boolean autoCommit) throws SQLException {
        delegate.setAutoCommit(autoCommit);
    }

    public void setReadOnly(boolean readOnly) throws SQLException {
        delegate.setReadOnly(readOnly);
    }

    public String getClientInfo(String name) throws SQLException {
        return delegate.getClientInfo(name);
    }

    public void setTransactionIsolation(int level) throws SQLException {
        delegate.setTransactionIsolation(level);
    }

    public void setTypeMap(Map<String, Class<?>> map) throws SQLException {
        delegate.setTypeMap(map);
    }

    public void setClientInfo(String name, String value) throws SQLClientInfoException {
        delegate.setClientInfo(name, value);
    }

    public PreparedStatement prepareStatement(String sql) throws SQLException {
        return delegate.prepareStatement(sql);
    }

    public boolean isClosed() throws SQLException {
        return delegate.isClosed();
    }

    public boolean isReadOnly() throws SQLException {
        return delegate.isReadOnly();
    }

    public Savepoint setSavepoint() throws SQLException {
        return delegate.setSavepoint();
    }

    public int getNetworkTimeout() throws SQLException {
        return delegate.getNetworkTimeout();
    }

    public String getSchema() throws SQLException {
        return delegate.getSchema();
    }

    public String getCatalog() throws SQLException {
        return delegate.getCatalog();
    }

    public void setCatalog(String catalog) throws SQLException {
        delegate.setCatalog(catalog);
    }

    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return delegate.isWrapperFor(iface);
    }

    public Statement createStatement(int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
        return delegate.createStatement(resultSetType, resultSetConcurrency, resultSetHoldability);
    }

    public void abort(Executor executor) throws SQLException {
        delegate.abort(executor);
    }

    public DatabaseMetaData getMetaData() throws SQLException {
        return delegate.getMetaData();
    }

    public SQLXML createSQLXML() throws SQLException {
        return delegate.createSQLXML();
    }

    public void rollback() throws SQLException {
        delegate.rollback();
    }

    public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
        return delegate.prepareCall(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
    }

    public void setNetworkTimeout(Executor executor, int milliseconds) throws SQLException {
        delegate.setNetworkTimeout(executor, milliseconds);
    }

    public SQLWarning getWarnings() throws SQLException {
        return delegate.getWarnings();
    }

    public boolean getAutoCommit() throws SQLException {
        return delegate.getAutoCommit();
    }

    public Clob createClob() throws SQLException {
        return delegate.createClob();
    }

    public <T> T unwrap(Class<T> iface) throws SQLException {
        return delegate.unwrap(iface);
    }

    public void setClientInfo(Properties properties) throws SQLClientInfoException {
        delegate.setClientInfo(properties);
    }

    public int getHoldability() throws SQLException {
        return delegate.getHoldability();
    }

    public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
        return delegate.prepareStatement(sql, resultSetType, resultSetConcurrency);
    }

    public String nativeSQL(String sql) throws SQLException {
        return delegate.nativeSQL(sql);
    }

    public Statement createStatement(int resultSetType, int resultSetConcurrency) throws SQLException {
        return delegate.createStatement(resultSetType, resultSetConcurrency);
    }

    public Savepoint setSavepoint(String name) throws SQLException {
        return delegate.setSavepoint(name);
    }

    public Properties getClientInfo() throws SQLException {
        return delegate.getClientInfo();
    }

    public PreparedStatement prepareStatement(String sql, String[] columnNames) throws SQLException {
        return delegate.prepareStatement(sql, columnNames);
    }

    public boolean isValid(int timeout) throws SQLException {
        return delegate.isValid(timeout);
    }

    public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
        return delegate.prepareCall(sql, resultSetType, resultSetConcurrency);
    }

    public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws SQLException {
        return delegate.prepareStatement(sql, autoGeneratedKeys);
    }

    public Blob createBlob() throws SQLException {
        return delegate.createBlob();
    }

    public PreparedStatement prepareStatement(String sql, int[] columnIndexes) throws SQLException {
        return delegate.prepareStatement(sql, columnIndexes);
    }

    public int getTransactionIsolation() throws SQLException {
        return delegate.getTransactionIsolation();
    }

    public CallableStatement prepareCall(String sql) throws SQLException {
        return delegate.prepareCall(sql);
    }

    public void clearWarnings() throws SQLException {
        delegate.clearWarnings();
    }

    public void setSchema(String schema) throws SQLException {
        delegate.setSchema(schema);
    }
}
