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

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

@EnableAutoConfiguration
public class ConfigurationByPropertiesTest extends AutoConfiguredMigrationScenario {

    @SpringBootConfiguration
    static class PropertiesConfiguration {

        @PostConstruct
        public void injectProperties() {
            System.setProperty("liquigraph.url", "jdbc:neo4j:" + neo4j.httpURI().toString());
        }

        @PreDestroy
        public void cleanUp() {
            System.clearProperty("liquigraph.url");
        }
    }
}
