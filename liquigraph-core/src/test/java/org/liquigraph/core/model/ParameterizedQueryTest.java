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

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class ParameterizedQueryTest {

    @Test
    public void equality_is_defined_on_query_and_parameters() {
        Query query1 = new ParameterizedQuery("MATCH (n {name: {1}}) RETURN n", singletonList("something"));
        Query query2 = new ParameterizedQuery("MATCH (n {name: {1}}) RETURN n", singletonList("something"));
        Query query3 = new ParameterizedQuery("MATCH (o {name: {1}}) RETURN o", singletonList("something"));
        Query query4 = new ParameterizedQuery("MATCH (n {name: {1}}) RETURN n", singletonList("something else"));

        assertThat(query1)
            .isEqualTo(query2)
            .isNotEqualTo(query3)
            .isNotEqualTo(query4);
    }
}
