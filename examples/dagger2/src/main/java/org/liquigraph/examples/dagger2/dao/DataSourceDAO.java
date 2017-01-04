package org.liquigraph.examples.dagger2.dao;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DataSourceDAO implements DAO {

    private DataSource dataSource;

    public DataSourceDAO(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public String executeQuery(String query) {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            if (!statement.execute(query)) {
                throw new RuntimeException("Could not execute query");
            }
            return this.extract("result", statement.getResultSet());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private String extract(String columnLabel, ResultSet results) throws SQLException {
        try (ResultSet resultSet = results) {
            resultSet.next();
            return resultSet.getString(columnLabel);
        }
    }

}
