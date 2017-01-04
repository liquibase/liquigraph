package org.liquigraph.examples.dagger2.di.module;

import dagger.Module;
import dagger.Provides;
import org.liquigraph.core.configuration.Configuration;
import org.liquigraph.examples.dagger2.liquigraph.Liquigraph;
import org.liquigraph.examples.dagger2.liquigraph.LiquigraphDatasource;

import javax.inject.Inject;
import javax.inject.Named;

@Module(includes = {LiquigraphConfigurationModule.class})
public class DataSourceLiquigraphModule {
    @Provides
    @Inject
    public Liquigraph dataSourceLiquigraph(@Named("datasource") Configuration configuration) {
        return new LiquigraphDatasource(configuration);
    }
}
