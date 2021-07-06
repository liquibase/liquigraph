/*
 * Copyright 2014-2020 the original author or authors.
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
package org.liquigraph.examples.spring;

import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
public class SampleController {

    private final Driver driver;

    public SampleController(Driver driver) {
        this.driver = driver;
    }

    @GetMapping(value = "/", produces = "text/plain")
    String home() {
        try (Session session = driver.session()) {
            return session.readTransaction(tx -> {
                Result result = tx.run("MATCH (n:Sentence) RETURN n.text AS result");
                return result.single().get("result").asString();
            });
        }
    }

    public static void main(String[] args) {
        SpringApplication.run(SampleController.class, args);
    }


    @Configuration
    static class DriverConfig {

        @Bean
        public Driver driver(@Value("${neo4j.url}") String uri,
                             @Value("${neo4j.username}") String username,
                             @Value("${neo4j.password}") String password) {

            return GraphDatabase.driver(uri, AuthTokens.basic(username, password));
        }
    }
}
