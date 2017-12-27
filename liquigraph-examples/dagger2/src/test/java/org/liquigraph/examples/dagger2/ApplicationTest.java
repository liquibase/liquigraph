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
package org.liquigraph.examples.dagger2;

import java.io.IOException;
import java.net.ServerSocket;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.neo4j.harness.junit.Neo4jRule;
import org.slf4j.bridge.SLF4JBridgeHandler;

import static io.restassured.RestAssured.get;
import static org.hamcrest.Matchers.containsString;

public class ApplicationTest {

    static {
        SLF4JBridgeHandler.removeHandlersForRootLogger();
    }

    @ClassRule
    public static final Neo4jRule neo4j = withVersionAwareConfig(new Neo4jRule());

    @Test
    public void service_responds_after_migration() {
        String jdbcUri = String.format("jdbc:neo4j:%s", neo4j.httpURI().toString());

        Application.main(jdbcUri);

        get("/")
                .then()
                .assertThat()
                .body(containsString("Hello world!"));
    }

    private static int availablePort() {
        try (ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private static Neo4jRule withVersionAwareConfig(Neo4jRule neo4jRule) {
        Neo4jVersion currentNeo4jVersion = new Neo4jVersionReader().read("/neo4j.properties");
        if (currentNeo4jVersion.compareTo(Neo4jVersion.parse("3.2")) < 0) {
            return neo4jRule
                .withConfig("dbms.connector.0.enabled", "false") /* BOLT */
                .withConfig("dbms.connector.1.address", "localhost:" + availablePort()); /* HTTP */
        }
        return neo4jRule
            .withConfig("dbms.connector.bolt.enabled", "false")
            .withConfig("dbms.connector.http.enabled", "true")
            .withConfig("dbms.connector.http.address", "localhost:" + availablePort());
    }

}
