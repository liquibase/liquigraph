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
package org.liquigraph.core.io;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.liquigraph.core.model.AndConditionQuery;
import org.liquigraph.core.model.CompoundConditionQuery;
import org.liquigraph.core.model.OrConditionQuery;
import org.liquigraph.core.model.Precondition;
import org.liquigraph.core.model.PreconditionErrorPolicy;
import org.liquigraph.core.model.ConditionQuery;
import org.liquigraph.core.model.SimpleConditionQuery;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

public class ConditionPrinterTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private ConditionPrinter conditionPrinter = new ConditionPrinter();

    @Test
    public void prints_nothing_for_null_precondition_query() {
        assertThat(conditionPrinter.print(null)).isEmpty();
    }

    @Test
    public void fails_to_print_when_given_an_unknown_query_type() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Unsupported query type <org.liquigraph.core.io.ConditionPrinterTest$1>");

        Precondition precondition = new Precondition();
        precondition.setQuery(new ConditionQuery() {
        });
        conditionPrinter.print(precondition);
    }

    @Test
    public void prints_simple_precondition_query() {
        Precondition precondition = precondition(
            simpleQuery("FROM a RETURN a"),
            PreconditionErrorPolicy.CONTINUE
        );

        String contents = String.join("\n", conditionPrinter.print(precondition));

        assertThat(contents).isEqualTo(
            "//Liquigraph precondition[if-not-met: CONTINUE]\n" +
                "FROM a RETURN a"
        );
    }

    @Test
    public void prints_compound_query() {
        Precondition precondition = precondition(
            orQuery(
                simpleQuery("FROM a RETURN a"),
                andQuery(
                    simpleQuery("FROM b RETURN b"),
                    simpleQuery("FROM c RETURN c")
                )
            ),
            PreconditionErrorPolicy.FAIL
        );

        String contents = String.join("\n", conditionPrinter.print(precondition));

        assertThat(contents).isEqualTo(
            "//Liquigraph precondition[if-not-met: FAIL]\n" +
                "((FROM a RETURN a) OR (((FROM b RETURN b) AND (FROM c RETURN c))))");
    }

    private Precondition precondition(ConditionQuery query, PreconditionErrorPolicy aContinue) {
        Precondition precondition = new Precondition();
        precondition.setQuery(query);
        precondition.setPolicy(aContinue);
        return precondition;
    }

    private CompoundConditionQuery orQuery(ConditionQuery query1, ConditionQuery query2) {
        OrConditionQuery orQuery = new OrConditionQuery();
        orQuery.setQueries(Arrays.asList(query1, query2));
        return orQuery;
    }

    private CompoundConditionQuery andQuery(ConditionQuery query1, ConditionQuery query2) {
        AndConditionQuery andQuery = new AndConditionQuery();
        andQuery.setQueries(Arrays.asList(query1, query2));
        return andQuery;
    }

    private SimpleConditionQuery simpleQuery(String query) {
        SimpleConditionQuery simpleQuery = new SimpleConditionQuery();
        simpleQuery.setQuery(query);
        return simpleQuery;
    }
}
