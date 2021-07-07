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

import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.Neo4jContainer;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class SampleControllerIT {

    private static final String SUPER_SECURE_PASSWORD = "s3cr3t";

    @Autowired
    MockMvc mockMvc;

    @ClassRule
    public static Neo4jContainer<?> container = new Neo4jContainer<>(neo4jVersion("4.3"))
        .withAdminPassword(SUPER_SECURE_PASSWORD);

    @DynamicPropertySource
    static void containerProperties(DynamicPropertyRegistry registry) {
        // Liquigraph own configuration properties build up on this - see main application.properties
        registry.add("neo4j.url", () -> container.getBoltUrl());
        registry.add("neo4j.username", () -> "neo4j");
        registry.add("neo4j.password", () -> SUPER_SECURE_PASSWORD);
    }

    @Test
    public void says_hello_world() throws Exception {
        mockMvc.perform(get("/").accept(MediaType.TEXT_PLAIN))
            .andExpect(status().is2xxSuccessful())
            .andExpect(content().string("Hello world!"));
    }

    private static String neo4jVersion(String defaultVersion) {
        String configuredVersion = System.getenv("NEO4J_VERSION");
        return String.format("neo4j:%s", configuredVersion == null ? defaultVersion : configuredVersion);
    }
}
