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
package org.liquigraph.extensions.jdbc;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;

import java.sql.Array;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.NClob;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Savepoint;
import java.sql.Statement;
import java.sql.Struct;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executor;

public class GraphDatabaseServiceConnection implements Connection {

    private final GraphDatabaseService graphDatabaseService;

    private Transaction transaction;

    private boolean closed;

    private boolean autoCommit;

    public GraphDatabaseServiceConnection(GraphDatabaseService graphDatabaseService) {
        this.graphDatabaseService = graphDatabaseService;
    }

    @Override
    public Statement createStatement() throws SQLException {
        checkIfClosed();
        transaction = this.graphDatabaseService.beginTx();
        return new GraphDatabaseServiceStatement(this.graphDatabaseService);
    }

    @Override
    public void commit() throws SQLException {
        checkIfClosed();
        if (transaction != null) {
            transaction.success();
        }
    }

    @Override
    public void rollback() throws SQLException {
        checkIfClosed();
        if (transaction != null) {
            transaction.failure();
        }
    }

    @Override
    public void close() {
        if (this.isClosed()) {
            return;
        }
        closed = true;
        if (transaction != null) {
            transaction.close();
            transaction = null;
        }
    }

    @Override
    public boolean isClosed() {
        return closed;
    }

    private void checkIfClosed() throws SQLException {
        if (this.isClosed()) {
            throw new SQLException("ResultSet is already closed");
        }
    }

    @Override
    public PreparedStatement prepareStatement(String sql) {
        return new GraphDatabaseServicePreparedStatement(graphDatabaseService, sql);
    }

    @Override
    public CallableStatement prepareCall(String sql) {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public String nativeSQL(String sql) {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public void setAutoCommit(boolean autoCommit) {
        this.autoCommit = autoCommit;
    }

    @Override
    public boolean getAutoCommit() {
        return this.autoCommit;
    }

    @Override
    public DatabaseMetaData getMetaData() {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public void setReadOnly(boolean readOnly) {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public boolean isReadOnly() {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public void setCatalog(String catalog) {
        throw new UnsupportedOperationException("not implemented");

    }

    @Override
    public String getCatalog() {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public void setTransactionIsolation(int level) {
        throw new UnsupportedOperationException("not implemented");

    }

    @Override
    public int getTransactionIsolation() {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public SQLWarning getWarnings() {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public void clearWarnings() {
        throw new UnsupportedOperationException("not implemented");

    }

    @Override
    public Statement createStatement(int resultSetType, int resultSetConcurrency) {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency) {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency) {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public Map<String, Class<?>> getTypeMap() {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public void setTypeMap(Map<String, Class<?>> map) {
        throw new UnsupportedOperationException("not implemented");

    }

    @Override
    public void setHoldability(int holdability) {
        throw new UnsupportedOperationException("not implemented");

    }

    @Override
    public int getHoldability() {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public Savepoint setSavepoint() {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public Savepoint setSavepoint(String name) {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public void rollback(Savepoint savepoint) {
        throw new UnsupportedOperationException("not implemented");

    }

    @Override
    public void releaseSavepoint(Savepoint savepoint) {
        throw new UnsupportedOperationException("not implemented");

    }

    @Override
    public Statement createStatement(int resultSetType, int resultSetConcurrency, int resultSetHoldability) {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int[] columnIndexes) {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public PreparedStatement prepareStatement(String sql, String[] columnNames) {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public Clob createClob() {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public Blob createBlob() {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public NClob createNClob() {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public SQLXML createSQLXML() {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public boolean isValid(int timeout) {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public void setClientInfo(String name, String value) {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public void setClientInfo(Properties properties) {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public String getClientInfo(String name) {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public Properties getClientInfo() {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public Array createArrayOf(String typeName, Object[] elements) {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public Struct createStruct(String typeName, Object[] attributes) {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public void setSchema(String schema) {
        throw new UnsupportedOperationException("not implemented");

    }

    @Override
    public String getSchema() {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public void abort(Executor executor) {
        throw new UnsupportedOperationException("not implemented");

    }

    @Override
    public void setNetworkTimeout(Executor executor, int milliseconds) {
        throw new UnsupportedOperationException("not implemented");

    }

    @Override
    public int getNetworkTimeout() {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public <T> T unwrap(Class<T> iface) {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) {
        throw new UnsupportedOperationException("not implemented");
    }
}
