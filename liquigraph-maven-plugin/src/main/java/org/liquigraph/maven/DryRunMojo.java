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

import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.liquigraph.core.configuration.ConfigurationBuilder;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Simulates Liquigraph execution and persists results in ${project.build.directory}/output.cypher.
 */
@Mojo(name = "dry-run", defaultPhase = LifecyclePhase.PREPARE_PACKAGE, threadSafe = true)
public class DryRunMojo extends LiquigraphMojoBase {

    @Override
    protected ConfigurationBuilder withExecutionMode(ConfigurationBuilder configurationBuilder) {
        Path outputDirectory = Paths.get(project.getBuild().getDirectory());
        return configurationBuilder.withDryRunMode(outputDirectory);
    }
}
