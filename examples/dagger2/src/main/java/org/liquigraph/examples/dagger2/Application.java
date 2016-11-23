package org.liquigraph.examples.dagger2;

import org.liquigraph.core.api.Liquigraph;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import static spark.Spark.*;

public class Application {

    public static void main(String[] args) throws Exception{
        MigrationComponent migrationComponent = org.liquigraph.examples.dagger2.DaggerMigrationComponent.create();
        // Running the migration
        new Liquigraph().runMigrations(migrationComponent.configuration());

        // Starting the web server
        port(8080);
        get("/", (request, response) -> {
            try (Connection connection = migrationComponent.dataSource().getConnection();
                 Statement statement = connection.createStatement()) {
                if (!statement.execute("MATCH (n:Sentence) RETURN n.text AS result")) {
                    throw new RuntimeException("Could not execute query");
                }
                return Application.extract("result", statement.getResultSet());
            }
        });
    }

    public static String extract(String columnLabel, ResultSet results) throws SQLException {
        try (ResultSet resultSet = results) {
            resultSet.next();
            return resultSet.getString(columnLabel);
        }
    }


}
