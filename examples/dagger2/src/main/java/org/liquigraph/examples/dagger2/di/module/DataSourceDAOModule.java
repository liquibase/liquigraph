package org.liquigraph.examples.dagger2.di.module;

import dagger.Module;
import dagger.Provides;
import org.liquigraph.examples.dagger2.dao.DAO;
import org.liquigraph.examples.dagger2.dao.DataSourceDAO;

import javax.inject.Singleton;
import javax.sql.DataSource;

@Module(includes = {DatasourceModule.class})
public class DataSourceDAOModule {

    @Provides
    @Singleton
    public DAO datasourceDAO(DataSource dataSource) {
        return new DataSourceDAO(dataSource);

    }
}
