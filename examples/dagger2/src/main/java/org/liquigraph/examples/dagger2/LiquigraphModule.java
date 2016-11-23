package org.liquigraph.examples.dagger2;

import dagger.Module;
import dagger.Provides;
import org.liquigraph.core.configuration.Configuration;
import org.liquigraph.core.configuration.ConfigurationBuilder;

import javax.sql.DataSource;


@Module
public class LiquigraphModule {

    @Provides
    static Configuration liquigraph(DataSource dataSource){
        return new ConfigurationBuilder()
                .withDataSource(dataSource)
                .withMasterChangelogLocation("changelog.xml")
                .withRunMode()
                .build();
    }

}
