/*
 * Copyright 2014-2020 the original author or authors.
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

import java.util.ArrayList;
import java.util.Collection;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Parameter;
import org.liquigraph.core.api.Liquigraph;
import org.liquigraph.core.configuration.Configuration;
import org.liquigraph.core.configuration.ConfigurationBuilder;

import static java.util.Collections.emptyList;

abstract class ChangelogExecutionMojoBase extends JdbcConnectionMojoBase {

    /**
     * Classpath location of the master changelog file
     */
    @Parameter(property = "changelog", required = true)
    String changelog;

    /**
     * Comma-separated execution context list.
     * If no context is specified, all Liquigraph changeset contexts will match.
     * If contexts are defined, all Liquigraph changesets whose at least 1 declared context will match.
     * Please note that Liquigraph changesets that define no context will always match.
     */
    @Parameter(property = "executionContexts", defaultValue = "")
    String executionContexts = "";

    @Override
    public final void execute() throws MojoExecutionException {
        try {
            Configuration configuration = withExecutionMode(new ConfigurationBuilder()
                .withChangelogLoader(ProjectChangelogLoader.getChangelogLoader(project))
                .withExecutionContexts(executionContexts(executionContexts))
                .withMasterChangelogLocation(changelog)
                .withDatabase(database)
                .withUsername(username)
                .withPassword(password)
                .withUri(jdbcUri))
                .build();

            new Liquigraph().runMigrations(configuration);
        }
        catch (Exception e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }
    }

    protected abstract ConfigurationBuilder withExecutionMode(ConfigurationBuilder configurationBuilder);

    private Collection<String> executionContexts(String executionContexts) {
        if (executionContexts.isEmpty()) {
            return emptyList();
        }
        Collection<String> result = new ArrayList<>();
        for (String context : executionContexts.split(",")) {
            result.add(context.trim());
        }
        return result;
    }

}
