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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.neo4j.harness.junit.rule.Neo4jRule;
import org.neo4j.jdbc.http.HttpDriver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;


@RunWith(SpringRunner.class)
@SpringBootTest
public class SampleControllerTest {

    @ClassRule
    public static Neo4jRule neo4jRule = new Neo4jRule();

    @Autowired
    private WebApplicationContext webContext;
    private MockMvc mockMvc;

    @BeforeClass
    public static void prepare() {
        System.setProperty("spring.datasource.url", "jdbc:neo4j:" + neo4jRule.httpURI());
        System.setProperty("spring.datasource.driver-class-name", HttpDriver.class.getName());
    }

    @AfterClass
    public static void cleanUp() {
        System.clearProperty("spring.datasource.url");
        System.clearProperty("spring.datasource.driver-class-name");
    }

    @Before
    public void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webContext).build();
    }

    @Test
    public void says_hello_world() throws Exception {
        mockMvc.perform(get("/").accept(MediaType.TEXT_PLAIN))
        .andExpect(status().is2xxSuccessful())
        .andExpect(content().string("Hello world!"));
    }
}
