/*
 * Copyright 2014-2021 the original author or authors.
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

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

public class OrQueryTest {

    @Test
    public void should_have_equality_on_queries() {
        assertThat(orQuery("MATCH (n) RETURN n", "MATCH(m) RETURN m"))
            .isEqualTo(orQuery("MATCH (n) RETURN n", "MATCH(m) RETURN m"))
            .isNotEqualTo(orQuery("MATCH (m) RETURN m", "MATCH(n) RETURN n"));
    }

    private static OrQuery orQuery(String firstQuery, String secondQuery) {
        OrQuery orQuery = new OrQuery();
        SimpleQuery firstSimpleQuery = new SimpleQuery();
        firstSimpleQuery.setQuery(firstQuery);
        SimpleQuery secondSimpleQuery = new SimpleQuery();
        secondSimpleQuery.setQuery(secondQuery);
        orQuery.setQueries(Arrays.asList(firstSimpleQuery, secondSimpleQuery));
        return orQuery;
    }
}
