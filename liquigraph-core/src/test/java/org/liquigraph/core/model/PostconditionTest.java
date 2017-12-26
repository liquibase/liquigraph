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

import com.google.common.testing.EqualsTester;
import org.junit.Test;

public class PostconditionTest {
    @Test
    public void should_have_equality_on_query() {
        new EqualsTester()
                .addEqualityGroup(
                        postcondition("MATCH (n) RETURN COUNT(n) > 0 AS result"),
                        postcondition("MATCH (n) RETURN COUNT(n) > 0 AS result"))
                .addEqualityGroup(postcondition("MATCH (m) RETURN COUNT(m) > 0 AS result"))
                .testEquals();
    }

    private static Postcondition postcondition(String query) {
        Postcondition postcondition = new Postcondition();
        SimpleQuery simpleQuery = new SimpleQuery();
        simpleQuery.setQuery(query);
        postcondition.setQuery(simpleQuery);
        return postcondition;
    }
}
