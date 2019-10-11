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
package org.liquigraph.extensions;

import org.junit.Rule;
import org.junit.Test;
import org.liquigraph.core.api.Liquigraph;
import org.liquigraph.core.configuration.ConfigurationBuilder;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;
import org.neo4j.harness.junit.Neo4jRule;
import org.slf4j.bridge.SLF4JBridgeHandler;

import java.util.HashMap;
import java.util.Map;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Java6Assertions.assertThat;

public class LiquigraphExtensionsTest {

    static {
        SLF4JBridgeHandler.removeHandlersForRootLogger();
    }

    @Rule
    public Neo4jRule neo4jRule = new Neo4jRule().withProcedure(LiquigraphExtensions.class);

    @Test
    public void returns_changelog_nodes() {
        Map<String, Object> expectedRow = new HashMap<>();
        expectedRow.put("id", "first-changelog");
        expectedRow.put("author", "fbiville");
        expectedRow.put("queries", singletonList("MATCH (n) RETURN n"));
        runMigrations("changelog/changelog.xml");

        GraphDatabaseService graphDb = neo4jRule.getGraphDatabaseService();
        try (Transaction transaction = graphDb.beginTx();
             Result result = graphDb.execute("CALL liquigraph.changelog()")) {

            assertThat(result).containsExactly(expectedRow);
            transaction.failure();
        }
    }

    private void runMigrations(String masterChangelog) {
        new Liquigraph().runMigrations(new ConfigurationBuilder()
          .withUri(jdbcUri())
          .withMasterChangelogLocation(masterChangelog)
          .withRunMode()
          .build());
    }

    private String jdbcUri() {
        return String.format("jdbc:neo4j:%s", neo4jRule.httpURI().toString());
    }
}
