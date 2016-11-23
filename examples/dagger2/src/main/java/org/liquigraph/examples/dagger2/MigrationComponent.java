package org.liquigraph.examples.dagger2;

import dagger.Component;
import org.liquigraph.core.configuration.Configuration;

import javax.sql.DataSource;

@Component(modules = {DatasourceModule.class,LiquigraphModule.class})
public interface MigrationComponent {

    Configuration configuration();

    DataSource dataSource();

}
