/*
 * Copyright 2014-2021 the original author or authors.
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
package org.liquigraph.cli;

import java.io.File;
import java.sql.ResultSet;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.liquigraph.testing.JdbcAwareGraphDatabase;
import org.liquigraph.testing.ParameterizedDatabaseIT;

import static org.assertj.core.api.Assertions.assertThat;

public class LiquigraphCliIT extends ParameterizedDatabaseIT {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    public LiquigraphCliIT(String description, JdbcAwareGraphDatabase graphDb, String uri) {
        super(description, graphDb, uri);
    }

    @Test
    public void dry_runs_migration() {
        LiquigraphCli.main(new String[] {
            "dry-run",
            "--changelog", "changelog.xml",
            "--graph-db-uri", uri,
            "--username", graphDb.username().get(),
            "--password", graphDb.password().get(),
            "--dry-run-output-directory", folder.getRoot().getPath()
        });

        assertThat(new File(folder.getRoot(), "output.cypher"))
            .hasContent(
                "//Liquigraph changeset[author: you, id: hello-world]\n"
                    + "//Liquigraph changeset[executionContexts: none declared]\n"
                    + "CREATE (n:Sentence {text:'Hello monde!'}) RETURN n\n"
                    + "//Liquigraph changeset[author: you, id: hello-world-fixed]\n"
                    + "//Liquigraph changeset[executionContexts: none declared]\n"
                    + "MATCH (n:Sentence {text:'Hello monde!'}) SET n.text='Hello world!' RETURN n");
    }

    @Test
    public void executes_migration() {
        LiquigraphCli.main(new String[] {
            "run",
            "--changelog", "changelog.xml",
            "--graph-db-uri", uri,
            "--username", graphDb.username().get(),
            "--password", graphDb.password().get()
        });

        graphDb.rollbackNewSingleStatementConnection(uri, statement -> {
            statement.execute("MATCH (s:Sentence) RETURN s.text AS text");
            try (ResultSet results = statement.getResultSet()) {
                assertThat(results.next()).as("Result set has exactly 1 result left").isTrue();
                assertThat(results.getString("text")).isEqualTo("Hello world!");
                assertThat(results.next()).as("Result set has no more result left").isFalse();
            }
        });
    }
}
