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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.liquigraph.examples.dagger2.di.component.MigrationComponent;
import org.neo4j.harness.ServerControls;
import org.neo4j.harness.TestServerBuilder;
import org.neo4j.harness.TestServerBuilders;

import static io.restassured.RestAssured.get;
import static org.hamcrest.Matchers.containsString;

public class MigrationWebIT {

    private ServerControls serverControls;

    @Before
    public void setUp() {
        TestServerBuilder testServerBuilder = TestServerBuilders.newInProcessBuilder();
        System.setProperty("java.io.tmpdir", "target/neo4j-hello-db");
        serverControls = testServerBuilder.newServer();
    }

    @After
    public void tearDown() {
        if (this.serverControls != null)
            this.serverControls.close();
    }

    @Test
    public void verifies_service_responds_after_migration() throws Exception {
        Application application = new Application();

        MigrationComponent migrationComponent = org.liquigraph.examples.dagger2.di.component.DaggerMigrationComponent.create();

        application.executeSimpleMigration(migrationComponent);

        get("/")
                .then()
                .assertThat()
                .body(containsString("Hello world!"));
    }


}
