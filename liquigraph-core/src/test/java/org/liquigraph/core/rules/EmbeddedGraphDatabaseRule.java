package org.liquigraph.core.rules;

import org.junit.rules.ExternalResource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import static com.google.common.base.Throwables.propagate;
import static java.nio.file.Files.walkFileTree;

public class EmbeddedGraphDatabaseRule extends ExternalResource {

    private final Path path;
    private final String uri;
    private Connection connection;

    public EmbeddedGraphDatabaseRule(String name) {
        try {
            this.path = Files.createTempDirectory(name);
            this.uri = "jdbc:neo4j:file:" + path.toFile().getAbsolutePath();
        } catch (IOException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    public Connection jdbcConnection() {
        return connection;
    }

    public String uri() {
        return uri;
    }

    protected void before() {
        try {
            Class.forName("org.neo4j.jdbc.Driver");
            connection = DriverManager.getConnection(uri);
            connection.setAutoCommit(false);
        } catch (ClassNotFoundException | SQLException e) {
            throw propagate(e);
        }
    }

    protected void after() {
        try {
            walkFileTree(path, new RecursiveDirectoryDeletionVisitor());
            if (!connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException | IOException e) {
            throw propagate(e);
        }
    }

}
