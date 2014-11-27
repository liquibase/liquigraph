package org.liquigraph.maven;

import org.liquigraph.core.configuration.ConfigurationBuilder;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

@Mojo(name = "run", defaultPhase = LifecyclePhase.PROCESS_RESOURCES, threadSafe = true)
public class RunMojo extends LiquigraphMojoBase {

    @Override
    protected ConfigurationBuilder withExecutionMode(ConfigurationBuilder configurationBuilder) {
        return configurationBuilder.withRunMode();
    }
}
