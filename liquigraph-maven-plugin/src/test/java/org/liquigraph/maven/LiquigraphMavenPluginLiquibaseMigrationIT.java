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

public class LiquigraphMavenPluginLiquibaseMigrationIT extends ParameterizedDatabaseIT {

    @Rule
    public MojoRule rule = new MojoRule();

    @Rule
    public TestResources resources = new TestResources();

    private Mojo runMojo;

    private Mojo migrateToLiquibaseMojo;

    private File migrationProjectBaseDir;

    public LiquigraphMavenPluginLiquibaseMigrationIT(String description, JdbcAwareGraphDatabase graphDb, String uri) {
        super(description, graphDb, uri);
    }

    @Before
    public void prepare() throws Exception {
        // using 2 different Maven projects is necessary because the JUnit Rule rejects the migration parameters, since
        // the run Mojo does not recognize them
        // the setup works only because they duplicate the Liquigraph change log and target the same database
        File projectBaseDir = new ProjectProcessor(resources).process("test-project", "pom.xml.tpl", substitutions());
        runMojo = configureMojo(new RunMojo(), projectBaseDir);
        migrationProjectBaseDir = new ProjectProcessor(resources).process("test-migration-project", "pom.xml.tpl", substitutions());
        migrateToLiquibaseMojo = configureMojo(new MigrateToLiquibaseMojo(), migrationProjectBaseDir);
    }

    @Test
    public void migrates_to_Liquibase_change_sets() throws Exception {
        runMojo.execute();

        migrateToLiquibaseMojo.execute();

        assertThat(Paths.get(migrationProjectBaseDir.getPath(), "target", "liquibase.xml"))
            .hasContent(
                "<?xml version=\"1.1\" encoding=\"UTF-8\" standalone=\"no\"?>\n" +
                    "<databaseChangeLog xmlns=\"http://www.liquibase.org/xml/ns/dbchangelog\" xmlns:ext=\"http://www.liquibase.org/xml/ns/dbchangelog-ext\" xmlns:pro=\"http://www.liquibase.org/xml/ns/pro\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.liquibase.org/xml/ns/dbchangelog-ext http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-ext.xsd http://www.liquibase.org/xml/ns/pro http://www.liquibase.org/xml/ns/pro/liquibase-pro-4.1.xsd http://www.liquibase.org/xml/ns/dbchangelog http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-4.1.xsd\">\n" +
                    "    <changeSet author=\"you\" id=\"hello-world\" objectQuotingStrategy=\"LEGACY\">\n" +
                    "        <sql splitStatements=\"true\" stripComments=\"false\">CREATE (n:Sentence {text:'Hello monde!'}) RETURN n</sql>\n" +
                    "    </changeSet>\n" +
                    "    <changeSet author=\"you\" id=\"hello-world-fixed\" objectQuotingStrategy=\"LEGACY\">\n" +
                    "        <sql splitStatements=\"true\" stripComments=\"false\">MATCH (n:Sentence {text:'Hello monde!'}) SET n.text='Hello world!' RETURN n</sql>\n" +
                    "    </changeSet>\n" +
                    "</databaseChangeLog>");

        graphDb.rollbackNewSingleStatementConnection(uri, statement -> {
            statement.execute("MATCH (n) WHERE ANY(label IN labels(n) WHERE label CONTAINS '__Liquigraph') RETURN count(n) AS count");
            try (ResultSet results = statement.getResultSet()) {
                assertThat(results.next()).isTrue();
                assertThat(results.getLong("count")).as("Liquigraph internal graph is gone").isEqualTo(0);
                assertThat(results.next()).isFalse();
            }
        });
        graphDb.rollbackNewSingleStatementConnection(uri, statement -> {
            statement.execute("MATCH (s:Sentence) RETURN count(s) AS count");
            try (ResultSet results = statement.getResultSet()) {
                assertThat(results.next()).isTrue();
                assertThat(results.getLong("count")).as("Change sets are not re-ran, just migrated").isEqualTo(1);
                assertThat(results.next()).isFalse();
            }
        });
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
