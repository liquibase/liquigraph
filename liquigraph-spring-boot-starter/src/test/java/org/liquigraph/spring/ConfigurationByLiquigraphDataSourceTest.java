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
package org.liquigraph.spring;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.h2.jdbcx.JdbcDataSource;
import org.liquigraph.spring.starter.LiquigraphDataSource;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

@EnableAutoConfiguration
public class ConfigurationByLiquigraphDataSourceTest extends AutoConfiguredMigrationScenario {

    @SpringBootConfiguration
    static class MultipleDataSource {

        @Primary @Bean
        public DataSource dataSource() {
            return new JdbcDataSource();
        }

        @LiquigraphDataSource @Bean
        public DataSource liquigraphDataSource() {
            HikariConfig configuration = new HikariConfig();
            configuration.setJdbcUrl("jdbc:neo4j:" + neo4j.httpURI());
            return new HikariDataSource(configuration);
        }
    }
}
