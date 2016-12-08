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

import org.junit.Rule;
import org.junit.Test;
import org.liquigraph.examples.dagger2.Application;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.factory.GraphDatabaseSettings;
import org.neo4j.io.fs.FileUtils;

import java.io.File;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.containsString;

public class MigrationWebIT {

    private static final File DB_PATH = new File( "target/neo4j-hello-db" );
    private GraphDatabaseService embeddedDatabase;

    @Test
    public void testMigration() throws Exception {

        Application.main();

        get("/")
        .then()
        .assertThat()
        .body(containsString("Hello World!"));

    }


}
