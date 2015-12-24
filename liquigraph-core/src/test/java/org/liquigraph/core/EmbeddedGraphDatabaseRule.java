package org.liquigraph.core;

import org.junit.rules.ExternalResource;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.test.TestGraphDatabaseFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.UUID;

import static com.google.common.base.Throwables.propagate;

public class EmbeddedGraphDatabaseRule extends ExternalResource {

    private final String dbName;
    private final String uri;
    private Connection connection;
    private GraphDatabaseService db;
    
    public EmbeddedGraphDatabaseRule(String name) {
        dbName = name + "-" + UUID.randomUUID().toString();
        uri = "jdbc:neo4j:instance:" + dbName;
    }
    
    public Connection jdbcConnection() {
        return connection;
    }

    public String uri() {
        return uri;
    }

    protected void before() {
        try {
            db = new TestGraphDatabaseFactory().newImpermanentDatabase();
            Class.forName("org.neo4j.jdbc.Driver");
            Properties props = properties();
            connection = DriverManager.getConnection(uri, props);
            connection.setAutoCommit(false);
        } catch (ClassNotFoundException | SQLException e) {
            throw propagate(e);
        }
    }

    protected void after() {
        try {
            if (!connection.isClosed()) {
                connection.close();
            }
            db.shutdown();
        } catch (SQLException e) {
            throw propagate(e);
        }
    }

    private Properties properties() {
        Properties props = new Properties();
        props.put(dbName, db);
        return props;
    }
}
