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
package org.liquigraph.spring.starter;

import javax.sql.DataSource;
import org.liquigraph.core.api.Liquigraph;
import org.liquigraph.spring.SpringChangelogLoader;
import org.liquigraph.spring.SpringLiquigraph;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ResourceLoader;

/**
 * {@link EnableAutoConfiguration Auto-configuration} for Liquigraph.
 *
 * @author Michael Vitz
 * @author Florent Biville
 */
@Configuration
@ConditionalOnClass(Liquigraph.class)
@ConditionalOnProperty(prefix = "liquigraph", name = "enabled", matchIfMissing = true)
@AutoConfigureAfter(DataSourceAutoConfiguration.class)
public class LiquigraphAutoConfiguration {

    @Configuration
    @ConditionalOnMissingBean(SpringLiquigraph.class)
    @EnableConfigurationProperties(LiquigraphProperties.class)
    public static class LiquigraphConfiguration {

        private final LiquigraphProperties properties;
        private final DataSource dataSource;
        private final DataSource liquigraphDataSource;

        public LiquigraphConfiguration(LiquigraphProperties properties,
                                       ObjectProvider<DataSource> dataSource,
                                       @LiquigraphDataSource ObjectProvider<DataSource> liquigraphDataSourceProvider) {
            this.properties = properties;
            this.dataSource = dataSource.getIfAvailable();
            this.liquigraphDataSource = liquigraphDataSourceProvider.getIfAvailable();
        }

        @Bean
        public SpringLiquigraph liquigraph(ResourceLoader loader) {
            SpringChangelogLoader changelogLoader = new SpringChangelogLoader(loader);
            return new SpringLiquigraph(
                getDataSource(),
                changelogLoader,
                properties.getChangeLog(),
                properties.getExecutionContexts()
            );
        }

        private DataSource getDataSource() {
            if (liquigraphDataSource != null) {
                return liquigraphDataSource;
            }
            String datasourceUrl = properties.getUrl();
            if (datasourceUrl != null) {
                return DataSourceBuilder
                    .create()
                        .url(datasourceUrl)
                        .username(properties.getUser())
                        .password(properties.getPassword())
                    .build();
            }
            return dataSource;
        }
    }
}
