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

import java.io.File;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

// Integration tests that do not require any database connectivity
public class LiquigraphMavenPluginOfflineIT {

    @Rule
    public MojoRule rule = new MojoRule();

    @Rule
    public TestResources resources = new TestResources();

    private Mojo migrateDeclaredChangeSetsMojo;

    private File projectBaseDir;

    @Before
    public void prepare() throws Exception {
        projectBaseDir = new ProjectProcessor(resources).process("test-project", "pom-migrate-declared-change-sets.xml.tpl");
        migrateDeclaredChangeSetsMojo = configureMojo(new MigrateDeclaredChangeSetsMojo(), projectBaseDir);
    }

    @Test
    public void migrates_to_Liquibase_change_sets() throws Exception {
        migrateDeclaredChangeSetsMojo.execute();

        assertThat(Paths.get(projectBaseDir.getPath(), "target", "changelog.liquibase.xml"))
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
    }

    private <T extends ProjectAwareMojo> T configureMojo(T mojo, File projectBaseDir) throws Exception {
        mojo.setProject(new ProjectStub(projectBaseDir));
        rule.configureMojo(mojo, "liquigraph-maven-plugin", new File(projectBaseDir, "pom.xml"));
        return mojo;
    }

}
