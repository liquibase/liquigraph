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
package org.liquigraph.spring;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import javax.sql.DataSource;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.After;
import org.junit.ClassRule;
import org.junit.Test;
import org.liquigraph.spring.starter.LiquigraphAutoConfiguration;
import org.liquigraph.spring.starter.LiquigraphDataSource;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Result;
import org.neo4j.harness.junit.Neo4jRule;
import org.slf4j.bridge.SLF4JBridgeHandler;
import org.springframework.boot.test.util.EnvironmentTestUtils;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import static org.assertj.core.api.Assertions.assertThat;

public class LiquigraphAutoconfigurationTest {

    static {
        SLF4JBridgeHandler.removeHandlersForRootLogger();
    }

    @ClassRule
    public static final Neo4jRule neo4j = new Neo4jRule();

    @After
    public void prepare() {
        try (Result ignored = graphDb().execute("MATCH (n) DETACH DELETE n")) { }
    }

    @Test
    public void runs_migrations_with_bean_configured_by_properties() {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext()) {
            loadContext(context, "liquigraph.url=" + jdbcUrl());

            assertThat(context.getBeansOfType(SpringLiquigraph.class))
                .as("SpringLiquigraph is configured through properties and default settings")
                .isNotEmpty();
            assertThatMigrationsHaveRun();
        }
    }

    @Test
    public void runs_migrations_with_changelog_property_alias() {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext()) {
            loadContext(
                context,
                "liquigraph.url=" + jdbcUrl(),
                "liquigraph.changelog=classpath:/db/liquigraph/changelog.xml"
            );

            assertThat(context.getBeansOfType(SpringLiquigraph.class))
            .as("SpringLiquigraph is configured through properties and default settings")
            .isNotEmpty();
            assertThatMigrationsHaveRun();
        }
    }

    @Test
    public void runs_migrations_with_bean_configured_by_a_single_data_source() {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext()) {
            loadContext(context, SingleDataSource.class);

            assertThat(context.getBeansOfType(SpringLiquigraph.class))
                .as("SpringLiquigraph is configured through a provided Datasource bean")
                .isNotEmpty();
            assertThatMigrationsHaveRun();
        }
    }

    @Test
    public void runs_migrations_with_bean_configured_by_Liquigraph_data_source() {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext()) {
            loadContext(context, MultipleDataSources.class);

            assertThat(context.getBeansOfType(SpringLiquigraph.class))
                .as("SpringLiquigraph is configured through a provided Datasource bean")
                .isNotEmpty();
            assertThatMigrationsHaveRun();
        }
    }

    @Test
    public void does_not_inject_bean_when_disabled() {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext()) {
            loadContext(context, "liquigraph.enabled=false");

            assertThat(context.getBeansOfType(SpringLiquigraph.class))
                .as("SpringLiquigraph is not injected when Liquigraph is disabled")
                .isEmpty();
            assertThatMigrationsHaveNotRun();
        }
    }

    @Test
    public void executes_only_configured_execution_contexts() {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext()) {
            loadContext(
                context,
                "liquigraph.changelog=classpath:/db/liquigraph/changelog-with-contexts.xml",
                "liquigraph.executionContexts=foo,bar",
                "liquigraph.url=" + jdbcUrl());

            assertThatMigrationsHaveRun();
        }
    }

    private static void loadContext(AnnotationConfigApplicationContext baseContext, Class<?> configuration, String... properties) {
        setUpEnvironment(baseContext, properties);
        baseContext.register(configuration);
        baseContext.refresh();
    }

    private static void loadContext(AnnotationConfigApplicationContext baseContext, String... properties) {
        setUpEnvironment(baseContext, properties);
        baseContext.refresh();
    }

    private static void setUpEnvironment(AnnotationConfigApplicationContext baseContext, String... properties) {
        for (String property : properties) {
            EnvironmentTestUtils.addEnvironment(baseContext, property);
        }
        baseContext.register(LiquigraphAutoConfiguration.class);
    }

    private static void assertThatMigrationsHaveRun() {
        try (Result result = graphDb().execute("MATCH (n:Sentence {text:'Hello world!'}) RETURN COUNT(n) AS count")) {
            assertThat(result.hasNext()).as("Query returns a count").isTrue();
            assertThat(result.next().get("count")).as("There is only 1 sentence in the graph").isEqualTo(1L);
            assertThat(result.hasNext()).as("No more counts are returned").isFalse();
        }
    }

    private static void assertThatMigrationsHaveNotRun() {
        try (Result result = graphDb().execute("MATCH (n) RETURN COUNT(n) AS count")) {
            assertThat(result.hasNext()).as("Query returns a count").isTrue();
            assertThat(result.next().get("count")).as("The graph is empty").isEqualTo(0L);
            assertThat(result.hasNext()).as("No more counts are returned").isFalse();
        }
    }

    private static GraphDatabaseService graphDb() {
        return neo4j.getGraphDatabaseService();
    }

    private static String jdbcUrl() {
        return "jdbc:neo4j:" + neo4j.httpURI().toString();
    }

    @Configuration
    static class SingleDataSource {


        @Bean
        public DataSource dataSource() {
            HikariConfig configuration = new HikariConfig();
            configuration.setJdbcUrl("jdbc:neo4j:" + neo4j.httpURI());
            return new HikariDataSource(configuration);
        }
    }

    @Configuration
    static class MultipleDataSources {

        @Primary
        @Bean
        public DataSource dataSource() {
            return new JdbcDataSource();
        }

        @LiquigraphDataSource
        @Bean
        public DataSource liquigraphDataSource() {
            HikariConfig configuration = new HikariConfig();
            configuration.setJdbcUrl("jdbc:neo4j:" + neo4j.httpURI());
            return new HikariDataSource(configuration);
        }
    }
}
