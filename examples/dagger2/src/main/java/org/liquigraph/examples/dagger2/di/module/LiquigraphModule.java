/*
 * Copyright 2014-2016 the original author or authors.
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
package org.liquigraph.examples.dagger2.di.module;


import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import dagger.Module;
import dagger.Provides;
import org.liquigraph.core.api.Liquigraph;
import org.liquigraph.core.configuration.Configuration;
import org.liquigraph.core.configuration.ConfigurationBuilder;
import org.liquigraph.examples.dagger2.dao.JdbcSentenceRepository;
import org.liquigraph.examples.dagger2.dao.SentenceRepository;

import javax.inject.Singleton;
import javax.sql.DataSource;

@Module
public class LiquigraphModule {
    @Provides
    public Liquigraph LiquigraphDatasource() {
        return new Liquigraph();
    }

    @Provides
    public Configuration liquigraphConfiguration(DataSource dataSource) {
        return new ConfigurationBuilder()
                .withDataSource(dataSource)
                .withMasterChangelogLocation("changelog.xml")
                .withRunMode()
                .build();
    }

    @Provides
    @Singleton
    static DataSource dataSource() {
        HikariConfig configuration = new HikariConfig("/datasource.properties");
        return new HikariDataSource(configuration);
    }

    @Provides
    @Singleton
    public SentenceRepository datasourceDAO(DataSource dataSource) {
        return new JdbcSentenceRepository(dataSource);

    }
}
