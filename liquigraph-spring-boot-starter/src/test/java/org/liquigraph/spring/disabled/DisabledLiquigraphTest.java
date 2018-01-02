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
package org.liquigraph.spring.disabled;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.liquigraph.spring.SpringLiquigraph;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.ResourceIterator;
import org.neo4j.graphdb.Transaction;
import org.neo4j.harness.junit.Neo4jRule;
import org.slf4j.bridge.SLF4JBridgeHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Java6Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
@EnableAutoConfiguration
@DirtiesContext
public class DisabledLiquigraphTest {

    static {
        SLF4JBridgeHandler.removeHandlersForRootLogger();
    }

    @ClassRule
    public static final Neo4jRule neo4j = new Neo4jRule();

    @Autowired
    SpringLiquigraph liquigraph;

    private GraphDatabaseService graphDb;

    @Before
    public void prepares() {
        graphDb = neo4j.getGraphDatabaseService();
    }

    @Test
    public void nothing_is_persisted_in_the_graph_when_Liquigraph_is_disabled() {
        try (Transaction ignored = graphDb.beginTx();
             ResourceIterator<Long> result = countNodes()) {

            assertThat(result)
                .as("Expects the graph to have 0 node since Liquigraph is disabled")
                .containsExactly(0L);
        }
    }

    @Test
    public void persists_nodes_after_an_explicit_call_to_run() {
        liquigraph.run();

        try (Transaction ignored = graphDb.beginTx();
             ResourceIterator<Long> result = countNodes()) {

            assertThat(result.hasNext()).as("Returns the count").isTrue();
            assertThat(result.next())
                .as("The graph is not empty after an explicit run")
                .isGreaterThan(0L);
            assertThat(result.hasNext()).as("Iterator is consumed").isFalse();
        }
    }

    @After
    public void cleans_up() {
        try (Transaction transaction = graphDb.beginTx()) {
            graphDb.execute("MATCH (n) DETACH DELETE n");
            transaction.success();
        }
    }

    private ResourceIterator<Long> countNodes() {
        return graphDb.execute("MATCH (n) RETURN COUNT(n) AS count").columnAs("count");
    }

    @SpringBootConfiguration
    static class Config {

        @PostConstruct
        public void injectProperties() {
            System.setProperty("liquigraph.url", "jdbc:neo4j:" + neo4j.httpURI().toString());
            System.setProperty("liquigraph.enabled", "false");
        }

        @PreDestroy
        public void cleanUp() {
            for (String property : System.getProperties().stringPropertyNames()) {
                if (property.startsWith("liquigraph.")) {
                    System.clearProperty(property);
                }
            }
        }
    }
}
