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
package org.liquigraph.maven;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.liquigraph.core.api.Liquigraph;
import org.liquigraph.core.configuration.Configuration;
import org.liquigraph.core.configuration.ConfigurationBuilder;

import java.util.ArrayList;
import java.util.Collection;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;

public abstract class LiquigraphMojoBase extends AbstractMojo {

    /**
     * Current maven project
     */
    @Parameter(property = "project", required = true, readonly = true)
    MavenProject project;

    /**
     * Classpath location of the master changelog file
     */
    @Parameter(property = "changelog", required = true)
    String changelog;

    /**
     * Graph JDBC URI
     * <ul>
     *  <li>jdbc:neo4j:http(s)://&lt;host&gt;:&lt;port&gt;/</li>
     *  <li>jdbc:neo4j:bolt://&lt;host&gt;:&lt;port&gt;/</li>
     * </ul>
     */
    @Parameter(property = "jdbcUri", required = true)
    String jdbcUri;

    /**
     * Graph connection username.
     */
    @Parameter(property = "username")
    String username;

    /**
     * Graph connection password.
     */
    @Parameter(property = "password")
    String password;

    /**
     * Comma-separated execution context list.
     * If no context is specified, all Liquigraph changeset contexts will match.
     * If contexts are defined, all Liquigraph changesets whose at least 1 declared context will match.
     * Please note that Liquigraph changesets that define no context will always match.
     */
    @Parameter(property = "executionContexts", defaultValue = "")
    String executionContexts = "";

    @Override
    public final void execute() throws MojoExecutionException, MojoFailureException {
        String directory = project.getBuild().getDirectory();

        try {
            Configuration configuration = withExecutionMode(new ConfigurationBuilder()
                .withClassLoader(ProjectClassLoader.getClassLoader(project))
                .withExecutionContexts(executionContexts(executionContexts))
                .withMasterChangelogLocation(changelog)
                .withUsername(username)
                .withPassword(password)
                .withUri(jdbcUri))
                .build();

            getLog().info("Generating Cypher output file in directory: " + directory);
            new Liquigraph().runMigrations(configuration);

        } catch (Exception e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }
    }

    protected abstract ConfigurationBuilder withExecutionMode(ConfigurationBuilder configurationBuilder);

    private Collection<String> executionContexts(String executionContexts) {
        if (executionContexts.isEmpty()) {
            return emptyList();
        }
        Collection<String> result = new ArrayList<>();
        for (String context : asList(executionContexts.split(","))) {
            result.add(context.trim());
        }
        return result;
    }

}
