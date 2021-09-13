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

import java.net.MalformedURLException;

import static org.liquigraph.maven.ChangeLogLoaders.changeLogLoader;
import static org.liquigraph.maven.ExecutionContexts.executionContexts;

@Mojo(name = "migrate-declared-change-sets", defaultPhase = LifecyclePhase.PREPARE_PACKAGE, threadSafe = true)
public class MigrateDeclaredChangeSetsMojo extends ProjectAwareMojo {

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

    private final Liquigraph liquigraph = new Liquigraph();


    @Override
    public void execute() throws MojoExecutionException {
        try {
            liquigraph.migrateDeclaredChangeSets(
                changelog,
                executionContexts(executionContexts),
                project.getBuild().getDirectory(),
                changeLogLoader(project)
            );
        } catch (DependencyResolutionRequiredException | MalformedURLException e) {
            throw new MojoExecutionException("Could not migrate declared change sets", e);
        }
    }
}
