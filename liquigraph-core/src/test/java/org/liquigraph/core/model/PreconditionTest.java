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

import static org.assertj.core.api.Assertions.assertThat;
import static org.liquigraph.core.model.PreconditionErrorPolicy.CONTINUE;
import static org.liquigraph.core.model.PreconditionErrorPolicy.MARK_AS_EXECUTED;

public class PreconditionTest {

    @Test
    public void should_have_equality_on_policy_and_query() {
        assertThat(precondition(MARK_AS_EXECUTED, "MATCH (n) RETURN COUNT(n) > 0 AS result"))
            .isEqualTo(precondition(MARK_AS_EXECUTED, "MATCH (n) RETURN COUNT(n) > 0 AS result"))
            .isNotEqualTo(precondition(CONTINUE, "MATCH (n) RETURN COUNT(n) > 0 AS result"))
            .isNotEqualTo(precondition(MARK_AS_EXECUTED, "MATCH (m) RETURN COUNT(m) > 0 AS result"));
    }

    private static Precondition precondition(PreconditionErrorPolicy policy, String query) {
        Precondition precondition = new Precondition();
        precondition.setPolicy(policy);
        SimpleQuery simpleQuery = new SimpleQuery();
        simpleQuery.setQuery(query);
        precondition.setQuery(simpleQuery);
        return precondition;
    }
}
