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

import com.google.common.collect.Lists;
import com.google.common.testing.EqualsTester;
import org.junit.Test;

public class OrQueryTest {
    @Test
    public void should_have_equality_on_queries() {
        new EqualsTester()
                .addEqualityGroup(
                        orQuery("MATCH (n) RETURN n", "MATCH(m) RETURN m"),
                        orQuery("MATCH (n) RETURN n", "MATCH(m) RETURN m"))
                .addEqualityGroup(orQuery("MATCH (m) RETURN m", "MATCH(n) RETURN n"))
                .testEquals();
    }

    private static OrQuery orQuery(String firstQuery, String secondQuery) {
        OrQuery orQuery = new OrQuery();
        SimpleQuery firstSimpleQuery = new SimpleQuery();
        firstSimpleQuery.setQuery(firstQuery);
        SimpleQuery secondSimpleQuery = new SimpleQuery();
        secondSimpleQuery.setQuery(secondQuery);
        orQuery.setQueries(Lists.<Query>newArrayList(firstSimpleQuery, secondSimpleQuery));
        return orQuery;
    }
}
