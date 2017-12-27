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
package org.liquigraph.examples.dagger2.configuration;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import dagger.Module;
import dagger.Provides;
import java.util.Optional;
import javax.inject.Singleton;
import javax.sql.DataSource;
import org.liquigraph.core.api.Liquigraph;
import org.liquigraph.core.configuration.Configuration;
import org.liquigraph.core.configuration.ConfigurationBuilder;

@Module
public class DataModule {

    private final Optional<String> jdbcUrl;

    public DataModule() {
        this(Optional.empty());
    }

    public DataModule(String jdbcUrl) {
        this(Optional.of(jdbcUrl));
    }

    private DataModule(Optional<String> jdbcUrl) {
        this.jdbcUrl = jdbcUrl;
    }

    @Provides
    public Liquigraph provideLiquigraph() {
        return new Liquigraph();
    }

    @Provides
    public Configuration provideLiquigraphConfiguration(DataSource dataSource) {
        return new ConfigurationBuilder()
                .withDataSource(dataSource)
                .withMasterChangelogLocation("changelog.xml")
                .withRunMode()
                .build();
    }

    @Provides
    @Singleton
    public DataSource provideDataSource() {
        return jdbcUrl.map(this::dataSourceFromUrl)
                      .orElseGet(this::dataSourceFromProperties);
    }

    private HikariDataSource dataSourceFromUrl(String url) {
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(url);
        return dataSource;
    }

    private HikariDataSource dataSourceFromProperties() {
        HikariConfig configuration = new HikariConfig("/datasource.properties");
        return new HikariDataSource(configuration);
    }
}
