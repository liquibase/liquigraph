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

import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.liquigraph.core.api.Liquigraph;
import org.liquigraph.core.configuration.Connections;

import java.io.File;
import java.net.MalformedURLException;
import java.util.Optional;

import static org.liquigraph.maven.ChangeLogLoaders.changeLogLoader;
import static org.liquigraph.maven.ExecutionContexts.executionContexts;

@Mojo(name = "migrate-to-liquibase", defaultPhase = LifecyclePhase.PREPARE_PACKAGE, threadSafe = true)
public class MigrateToLiquibaseMojo extends JdbcConnectionMojoBase {

    /**
     * Classpath location of the main change log file
     */
    @Parameter(property = "changelog", required = true)
    String changelog;

    /**
     * Comma-separated execution context list. If no context is specified, all Liquigraph change sets contexts will
     * match. If contexts are defined, the Liquigraph change sets without any contexts or with least 1 matching declared
     * context will match.
     */
    @Parameter(property = "executionContexts", defaultValue = "")
    String executionContexts = "";

    /**
     * Resulting XML file, where the Liquibase change sets are written to
     */
    @Parameter(property = "liquibaseFileName", defaultValue = "liquibase.xml")
    String liquibaseFileName;

    /**
     * Whether to delete the Liquigraph graph once the migration to Liquibase is complete
     */
    @Parameter(property = "deleteLiquigraphGraph")
    boolean deleteLiquigraphGraph;

    private final Liquigraph liquigraph = new Liquigraph();


    @Override
    public void execute() throws MojoExecutionException {
        try {
            liquigraph.migrateDeclaredChangeSets(
                changelog,
                executionContexts(executionContexts),
                new File(project.getBuild().getDirectory(), liquibaseFileName),
                changeLogLoader(project)
            );
            liquigraph.migratePersistedChangeSets(
                Connections.provide(
                    Optional.of(jdbcUri),
                    Optional.ofNullable(database),
                    Optional.ofNullable(username),
                    Optional.ofNullable(password),
                    Optional.empty()
                ),
                liquibaseFileName,
                deleteLiquigraphGraph
            );
        } catch (DependencyResolutionRequiredException | MalformedURLException e) {
            throw new MojoExecutionException("Could not migrate declared change sets", e);
        }
    }
}
