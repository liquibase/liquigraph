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
package org.liquigraph.spring;

import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Result;
import org.neo4j.harness.junit.Neo4jRule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
public abstract class AutoConfiguredMigrationScenario {

    @ClassRule
    public static final Neo4jRule neo4j = new Neo4jRule();

    @Autowired
    SpringLiquigraph liquigraph;

    @Test
    public void performs_auto_configured_migration() {
        GraphDatabaseService graphDb = neo4j.getGraphDatabaseService();
        try (Result result = graphDb.execute("MATCH (n:Sentence {text:'Hello world!'}) RETURN COUNT(n) AS count")) {
            assertThat(result.hasNext()).isTrue();
            assertThat(result.next().get("count")).isEqualTo(1L);
            assertThat(result.hasNext()).isFalse();
        }
    }
}
