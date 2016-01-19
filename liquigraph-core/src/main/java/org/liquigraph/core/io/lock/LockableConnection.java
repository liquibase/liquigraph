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
package org.liquigraph.core.io.lock;

import org.liquigraph.core.exception.LiquigraphLockException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import java.util.UUID;
import java.util.concurrent.Executor;

/**
 * This JDBC connection decorator writes a (:__LiquigraphLock)
 * Neo4j node in order to prevent concurrent executions.
 *
 * Closing this connection will delete the created "Lock" node.
 *
 * A shutdown hook is executed to remove the lock node if and
 * only if the connection has not been properly closed.
 */
public final class LockableConnection implements Connection {

    private static final Logger LOGGER = LoggerFactory.getLogger(LockableConnection.class);
    private final Connection delegate;
    private final UUID uuid;
    private final Thread task;

    private LockableConnection(Connection delegate) {
        this.delegate = delegate;
        this.uuid = UUID.randomUUID();
        this.task = new Thread(new ShutdownTask(this));
    }

    public static LockableConnection acquire(Connection delegate) {
        LockableConnection connection = new LockableConnection(delegate);
        try {
            connection.acquireLock();
            return connection;
        } catch (RuntimeException e) {
            try {
                connection.close();
            } catch (SQLException exception) {
                e.addSuppressed(exception);
            }
            throw e;
        }
    }

    /**
     * Removes lock node and removes the related cleanup
     * task shutdown hook before closing the underlying
     * connection.
     *
     * @see ShutdownTask
     */
    @Override
    public void close() throws SQLException {
        removeShutdownHook();
        releaseLock();
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

    final void releaseLock() {
        try (PreparedStatement statement = prepareStatement(
            "MATCH (lock:__LiquigraphLock {uuid:{1}}) DELETE lock")) {

            statement.setString(1, uuid.toString());
            statement.execute();
            commit();
        } catch (SQLException e) {
            LOGGER.error(
                "Cannot remove __LiquigraphLock during cleanup.",
                e
            );
        }
    }

    private final void acquireLock() {
        addShutdownHook();
        ensureLockUnicity();
        tryWriteLock();
    }

    private final void addShutdownHook() {
        Runtime.getRuntime().addShutdownHook(task);
    }

    private final void removeShutdownHook() {
        Runtime.getRuntime().removeShutdownHook(task);
    }

    private final void ensureLockUnicity() {
        try (Statement statement = delegate.createStatement()) {
            statement.execute("CREATE CONSTRAINT ON (lock:__LiquigraphLock) ASSERT lock.name IS UNIQUE");
            commit();
        }
        catch (SQLException e) {
            throw new LiquigraphLockException(
                "Could not ensure __LiquigraphLock unicity\n\t" +
                    "Please make sure your instance is in a clean state\n\t" +
                    "No more than 1 lock should be there simultaneously!",
                e
            );
        }
    }

    private final void tryWriteLock() {
        try (PreparedStatement statement = delegate.prepareStatement(
            "CREATE (:__LiquigraphLock {name:'John', uuid:{1}})")) {

            statement.setString(1, uuid.toString());
            statement.execute();
            commit();
        }
        catch (SQLException e) {
            throw new LiquigraphLockException(
                "Cannot create __LiquigraphLock lock\n\t" +
                "Likely another Liquigraph execution is going on or has crashed.",
                e
            );
        }
    }
}
