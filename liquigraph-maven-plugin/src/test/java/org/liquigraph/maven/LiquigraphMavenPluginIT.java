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
package org.liquigraph.maven;

import org.apache.maven.plugin.Mojo;
import org.apache.maven.plugin.testing.MojoRule;
import org.apache.maven.plugin.testing.resources.TestResources;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.liquigraph.testing.JdbcAwareGraphDatabase;
import org.liquigraph.testing.ParameterizedDatabaseIT;

import java.io.File;
import java.nio.file.Paths;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class LiquigraphMavenPluginIT extends ParameterizedDatabaseIT {

    @Rule
    public MojoRule rule = new MojoRule();

    @Rule
    public TestResources resources = new TestResources();

    private Mojo runMojo;

    private Mojo dryRunMojo;

    private File projectBaseDir;

    public LiquigraphMavenPluginIT(String description, JdbcAwareGraphDatabase graphDb, String uri) {
        super(description, graphDb, uri);
    }

    @Before
    public void prepare() throws Exception {
        projectBaseDir = new ProjectProcessor(resources).process("test-project", "pom.xml.tpl", substitutions());
        runMojo = configureMojo(new RunMojo(), projectBaseDir);
        dryRunMojo = configureMojo(new DryRunMojo(), projectBaseDir);
    }

    @Test
    public void runs_migrations() throws Exception {
        runMojo.execute();

        graphDb.rollbackNewSingleStatementConnection(uri, statement -> {
            statement.execute("MATCH (s:Sentence) RETURN s.text AS text");
            try (ResultSet results = statement.getResultSet()) {
                assertThat(results.next()).as("Result set has exactly 1 result left").isTrue();
                assertThat(results.getString("text")).isEqualTo("Hello world!");
                assertThat(results.next()).as("Result set has no more result left").isFalse();
            }
        });
    }

    @Test
    public void dry_runs_migrations() throws Exception {
        dryRunMojo.execute();

        assertThat(Paths.get(projectBaseDir.getPath(), "target", "output.cypher"))
            .hasContent(
                "//Liquigraph changeset[author: you, id: hello-world]\n"
                    + "//Liquigraph changeset[executionContexts: none declared]\n"
                    + "CREATE (n:Sentence {text:'Hello monde!'}) RETURN n\n"
                    + "//Liquigraph changeset[author: you, id: hello-world-fixed]\n"
                    + "//Liquigraph changeset[executionContexts: none declared]\n"
                    + "MATCH (n:Sentence {text:'Hello monde!'}) SET n.text='Hello world!' RETURN n");
    }

    private <T extends ProjectAwareMojo> T configureMojo(T mojo, File projectBaseDir) throws Exception {
        mojo.setProject(new ProjectStub(projectBaseDir));
        rule.configureMojo(mojo, "liquigraph-maven-plugin", new File(projectBaseDir, "pom.xml"));
        return mojo;
    }

    private Map<String, String> substitutions() {
        Map<String, String> substitutions = new HashMap<>(2);
        substitutions.put("__JDBC_URI__", uri);
        substitutions.put("__JDBC_ADMIN_PASSWORD__", graphDb.password().get());
        return substitutions;
    }

}
