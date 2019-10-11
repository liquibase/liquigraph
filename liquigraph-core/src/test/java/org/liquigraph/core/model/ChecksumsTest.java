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
package org.liquigraph.core.model;

import org.junit.Test;

import java.util.Collections;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.liquigraph.core.model.Checksums.checksum;

public class ChecksumsTest {

    @Test
    public void computes_checksum_of_queries() {
        Query query1 = new SimpleQuery("MATCH (n) RETURN n");
        Query query2 = new SimpleQuery("MATCH (m) RETURN m");
        assertThat(checksum(asList(query1, query2))).isEqualTo("9c68d381cf24b9cab5843a506229c5dee1083f8e");
    }

    @Test
    public void computes_checksum_of_parameterized_queries() {
        Query query = new ParameterizedQuery("MATCH (n {name: {1}}) RETURN n", singletonList("some-name"));
        assertThat(checksum(singletonList(query))).isEqualTo("a4785eefdb5f10e155648710bb0e26cb13941e7c");
    }

    @Test
    public void avoids_checksum_collisions_of_differing_queries() {
        Query query1 = new ParameterizedQuery("MATCH (n {name: {1}}) RETURN n", singletonList("some-name"));
        Query query2 = new ParameterizedQuery("MATCH (n {name: {1}}) RETURN n", singletonList("some-name"));
        Query query3 = new ParameterizedQuery("MATCH (m {name: {1}}) RETURN m", singletonList("some-name"));
        Query query4 = new SimpleQuery("MATCH (n) RETURN n");

        assertThat(checksum(singletonList(query1)))
            .isEqualTo(checksum(singletonList(query2)))
            .isNotEqualTo(checksum(singletonList(query3)))
            .isNotEqualTo(checksum(singletonList(query4)));
    }
}
