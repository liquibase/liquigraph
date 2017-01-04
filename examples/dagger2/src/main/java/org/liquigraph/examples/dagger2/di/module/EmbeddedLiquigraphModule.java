package org.liquigraph.examples.dagger2.di.module;

import dagger.Module;
import dagger.Provides;
import org.liquigraph.core.configuration.Configuration;
import org.liquigraph.examples.dagger2.liquigraph.Liquigraph;
import org.liquigraph.examples.dagger2.liquigraph.LiquigraphEmbedded;

import javax.inject.Inject;
import javax.inject.Named;

@Module(includes = {LiquigraphConfigurationModule.class})
public class EmbeddedLiquigraphModule {

    @Inject
    @Provides
    public Liquigraph liquigraphEmbedded(@Named("embedded") Configuration configuration) {
        return new LiquigraphEmbedded(configuration);
    }
}
