/*
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
package org.liquigraph.examples.dagger2;

import org.liquigraph.core.api.Liquigraph;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import static spark.Spark.*;

public class Application {

    public static void main(String ... args) throws Exception{
        MigrationComponent migrationComponent = org.liquigraph.examples.dagger2.DaggerMigrationComponent.create();
        // Running the migration
        migrationComponent.liquigraphEmbedded();

        // Starting the web server
        port(8080);
        get("/", (request, response) -> {
            //TOOD : ajouter un DAO
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
