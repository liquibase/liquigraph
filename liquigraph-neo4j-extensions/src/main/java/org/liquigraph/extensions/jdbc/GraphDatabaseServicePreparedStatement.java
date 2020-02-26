package org.liquigraph.extensions.jdbc;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.Date;
import java.sql.NClob;
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GraphDatabaseServicePreparedStatement implements PreparedStatement {

    private static final Pattern PARAMETER_REGEX = Pattern.compile("\\{\\d+}");

    private final GraphDatabaseService graphDb;

    private final int parameterCount;

    private final String rawStatement;

    private final String[] parameters;

    private boolean closed;

    public GraphDatabaseServicePreparedStatement(GraphDatabaseService graphDb, String rawStatement) {
        this.graphDb = graphDb;
        this.parameterCount = countParameters(rawStatement);
        this.rawStatement = rawStatement;
        this.parameters = new String[parameterCount];
    }

    @Override
    public void setString(int parameterIndex, String parameter) throws SQLException {
        this.checkIfClosed();
        if (parameterIndex > parameterCount) {
            String message = String.format("cannot set parameter %d, only %d parameter(s) in statement", parameterIndex, parameterCount);
            throw new SQLException(message);
        }
        this.parameters[parameterIndex - 1] = parameter;
    }

    @Override
    public boolean execute() throws SQLException {
        this.checkIfClosed();
        try (Transaction transaction = graphDb.beginTx()) {
            graphDb.execute(rawStatement, asMap(parameters));
            transaction.success();
        }
        return false; // TODO: figure out how to return expected value
    }

    @Override
    public void close() {
        if (isClosed()) {
            return;
        }
        closed = true;
    }

    @Override
    public boolean isClosed() {
        return closed;
    }

    @Override
    public ResultSet executeQuery() throws SQLException {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public int executeUpdate() throws SQLException {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public void setNull(int parameterIndex, int sqlType) throws SQLException {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public void setBoolean(int parameterIndex, boolean x) throws SQLException {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public void setByte(int parameterIndex, byte x) throws SQLException {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public void setShort(int parameterIndex, short x) throws SQLException {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public void setInt(int parameterIndex, int x) throws SQLException {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public void setLong(int parameterIndex, long x) throws SQLException {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public void setFloat(int parameterIndex, float x) throws SQLException {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public void setDouble(int parameterIndex, double x) throws SQLException {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public void setBigDecimal(int parameterIndex, BigDecimal x) throws SQLException {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public void setBytes(int parameterIndex, byte[] x) throws SQLException {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public void setDate(int parameterIndex, Date x) throws SQLException {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public void setTime(int parameterIndex, Time x) throws SQLException {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public void setTimestamp(int parameterIndex, Timestamp x) throws SQLException {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public void setAsciiStream(int parameterIndex, InputStream x, int length) throws SQLException {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public void setUnicodeStream(int parameterIndex, InputStream x, int length) throws SQLException {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public void setBinaryStream(int parameterIndex, InputStream x, int length) throws SQLException {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public void clearParameters() throws SQLException {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public void setObject(int parameterIndex, Object x, int targetSqlType) throws SQLException {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public void setObject(int parameterIndex, Object x) throws SQLException {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public void addBatch() throws SQLException {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public void setCharacterStream(int parameterIndex, Reader reader, int length) throws SQLException {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public void setRef(int parameterIndex, Ref x) throws SQLException {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public void setBlob(int parameterIndex, Blob x) throws SQLException {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public void setClob(int parameterIndex, Clob x) throws SQLException {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public void setArray(int parameterIndex, Array x) throws SQLException {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public ResultSetMetaData getMetaData() throws SQLException {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public void setDate(int parameterIndex, Date x, Calendar cal) throws SQLException {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public void setTime(int parameterIndex, Time x, Calendar cal) throws SQLException {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public void setTimestamp(int parameterIndex, Timestamp x, Calendar cal) throws SQLException {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public void setNull(int parameterIndex, int sqlType, String typeName) throws SQLException {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public void setURL(int parameterIndex, URL x) throws SQLException {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public ParameterMetaData getParameterMetaData() throws SQLException {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public void setRowId(int parameterIndex, RowId x) throws SQLException {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public void setNString(int parameterIndex, String value) throws SQLException {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public void setNCharacterStream(int parameterIndex, Reader value, long length) throws SQLException {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public void setNClob(int parameterIndex, NClob value) throws SQLException {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public void setClob(int parameterIndex, Reader reader, long length) throws SQLException {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public void setBlob(int parameterIndex, InputStream inputStream, long length) throws SQLException {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public void setNClob(int parameterIndex, Reader reader, long length) throws SQLException {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public void setSQLXML(int parameterIndex, SQLXML xmlObject) throws SQLException {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public void setObject(int parameterIndex, Object x, int targetSqlType, int scaleOrLength) throws SQLException {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public void setAsciiStream(int parameterIndex, InputStream x, long length) throws SQLException {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public void setBinaryStream(int parameterIndex, InputStream x, long length) throws SQLException {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public void setCharacterStream(int parameterIndex, Reader reader, long length) throws SQLException {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public void setAsciiStream(int parameterIndex, InputStream x) throws SQLException {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public void setBinaryStream(int parameterIndex, InputStream x) throws SQLException {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public void setCharacterStream(int parameterIndex, Reader reader) throws SQLException {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public void setNCharacterStream(int parameterIndex, Reader value) throws SQLException {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public void setClob(int parameterIndex, Reader reader) throws SQLException {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public void setBlob(int parameterIndex, InputStream inputStream) throws SQLException {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public void setNClob(int parameterIndex, Reader reader) throws SQLException {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public ResultSet executeQuery(String sql) throws SQLException {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public int executeUpdate(String sql) throws SQLException {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public int getMaxFieldSize() throws SQLException {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public void setMaxFieldSize(int max) throws SQLException {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public int getMaxRows() throws SQLException {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public void setMaxRows(int max) throws SQLException {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public void setEscapeProcessing(boolean enable) throws SQLException {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public int getQueryTimeout() throws SQLException {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public void setQueryTimeout(int seconds) throws SQLException {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public void cancel() throws SQLException {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public SQLWarning getWarnings() throws SQLException {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public void clearWarnings() throws SQLException {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public void setCursorName(String name) throws SQLException {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public boolean execute(String sql) throws SQLException {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public ResultSet getResultSet() throws SQLException {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public int getUpdateCount() throws SQLException {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public boolean getMoreResults() throws SQLException {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public void setFetchDirection(int direction) throws SQLException {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public int getFetchDirection() throws SQLException {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public void setFetchSize(int rows) throws SQLException {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public int getFetchSize() throws SQLException {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public int getResultSetConcurrency() throws SQLException {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public int getResultSetType() throws SQLException {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public void addBatch(String sql) throws SQLException {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public void clearBatch() throws SQLException {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public int[] executeBatch() throws SQLException {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public Connection getConnection() throws SQLException {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public boolean getMoreResults(int current) throws SQLException {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public ResultSet getGeneratedKeys() throws SQLException {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public int executeUpdate(String sql, int autoGeneratedKeys) throws SQLException {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public int executeUpdate(String sql, int[] columnIndexes) throws SQLException {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public int executeUpdate(String sql, String[] columnNames) throws SQLException {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public boolean execute(String sql, int autoGeneratedKeys) throws SQLException {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public boolean execute(String sql, int[] columnIndexes) throws SQLException {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public boolean execute(String sql, String[] columnNames) throws SQLException {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public int getResultSetHoldability() throws SQLException {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public void setPoolable(boolean poolable) throws SQLException {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public boolean isPoolable() throws SQLException {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public void closeOnCompletion() throws SQLException {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public boolean isCloseOnCompletion() throws SQLException {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        throw new UnsupportedOperationException("not implemented");
    }

    private int countParameters(String rawStatement) {
        int count = 0;
        Matcher matcher = PARAMETER_REGEX.matcher(rawStatement);
        while (matcher.find()) {
            count++;
        }
        return count;
    }

    private Map<String, Object> asMap(String[] parameters) {
        Map<String, Object> result = new HashMap<>(parameters.length, 1.0f);
        for (int i = 0; i < parameters.length; i++) {
            String parameter = parameters[i];
            result.put(String.format("%d", i + 1), parameter);
        }
        return result;
    }

    private void checkIfClosed() throws SQLException {
        if (this.isClosed()) {
            throw new SQLException("Prepared statement is closed");
        }
    }
}
