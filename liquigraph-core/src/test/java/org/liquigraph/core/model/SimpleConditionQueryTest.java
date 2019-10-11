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

import static org.assertj.core.api.Assertions.assertThat;

public class SimpleConditionQueryTest {

    @Test
    public void should_have_equality_on_query() {
        assertThat(simpleQuery("MATCH (n) RETURN n"))
            .isEqualTo(simpleQuery("MATCH (n) RETURN n"))
            .isNotEqualTo(simpleQuery("MATCH (m) RETURN m"));
    }

    private static SimpleConditionQuery simpleQuery(String query) {
        SimpleConditionQuery simpleQuery = new SimpleConditionQuery();
        simpleQuery.setQuery(query);
        return simpleQuery;
    }
}
