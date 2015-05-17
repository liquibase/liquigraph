package org.liquigraph.maven;

import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.liquigraph.core.configuration.ConfigurationBuilder;

/**
 * Triggers Liquigraph execution.
 */
@Mojo(name = "run", defaultPhase = LifecyclePhase.PROCESS_RESOURCES, threadSafe = true)
public class RunMojo extends LiquigraphMojoBase {

    @Override
    protected ConfigurationBuilder withExecutionMode(ConfigurationBuilder configurationBuilder) {
        return configurationBuilder.withRunMode();
    }
}
