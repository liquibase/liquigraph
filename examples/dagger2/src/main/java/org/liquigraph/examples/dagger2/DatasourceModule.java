package org.liquigraph.examples.dagger2;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import dagger.Module;
import dagger.Provides;

import javax.sql.DataSource;

@Module
public class DatasourceModule {

    @Provides static DataSource dataSource(){
        HikariConfig configuration = new HikariConfig("/datasource.properties");
        return new HikariDataSource(configuration);
    }
}
