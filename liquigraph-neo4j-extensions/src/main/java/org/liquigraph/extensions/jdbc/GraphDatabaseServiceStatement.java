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
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;

public class GraphDatabaseServiceStatement implements Statement {

    private final GraphDatabaseService graphDatabaseService;

    private Transaction currentTransaction = null;

    private boolean closed = false;

    public GraphDatabaseServiceStatement(GraphDatabaseService graphDatabaseService) {
        this.graphDatabaseService = graphDatabaseService;
    }

    @Override
    public boolean execute(String sql) throws SQLException {
        executeQuery(sql);
        return false; // TODO: figure out how to return expected value
    }

    @Override
    public ResultSet executeQuery(String sql) throws SQLException {
        checkIfClosed();
        currentTransaction = this.graphDatabaseService.beginTx();
        Result result = this.graphDatabaseService.execute(sql);
        currentTransaction.success();
        return convert(result);
    }

    @Override
    public void close() {
        if (isClosed()) {
            return;
        }
        closed = true;
        if (currentTransaction != null) {
            currentTransaction.close();
            currentTransaction = null;
        }
    }

    @Override
    public boolean isClosed() {
        return closed;
    }

    private void checkIfClosed() throws SQLException {
        if (this.isClosed()) {
            throw new SQLException("Statement is already closed");
        }
    }

    @Override
    public int executeUpdate(String sql) {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public int getMaxFieldSize() {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public void setMaxFieldSize(int max) {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public int getMaxRows() {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public void setMaxRows(int max) {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public void setEscapeProcessing(boolean enable) {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public int getQueryTimeout() {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public void setQueryTimeout(int seconds) {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public void cancel() {
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
    public void setCursorName(String name) {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public ResultSet getResultSet() {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public int getUpdateCount() {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public boolean getMoreResults() {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public void setFetchDirection(int direction) {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public int getFetchDirection() {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public void setFetchSize(int rows) {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public int getFetchSize() {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public int getResultSetConcurrency() {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public int getResultSetType() {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public void addBatch(String sql) {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public void clearBatch() {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public int[] executeBatch() {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public Connection getConnection() {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public boolean getMoreResults(int current) {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public ResultSet getGeneratedKeys() {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public int executeUpdate(String sql, int autoGeneratedKeys) {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public int executeUpdate(String sql, int[] columnIndexes) {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public int executeUpdate(String sql, String[] columnNames) {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public boolean execute(String sql, int autoGeneratedKeys) {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public boolean execute(String sql, int[] columnIndexes) {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public boolean execute(String sql, String[] columnNames) {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public int getResultSetHoldability() {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public void setPoolable(boolean poolable) {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public boolean isPoolable() {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public void closeOnCompletion() {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public boolean isCloseOnCompletion() {
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

    private ResultSet convert(Result result) {
        return new GraphDatabaseServiceResultSet(result);
    }
}
