/*
 * Copyright 2014-2022 the original author or authors.
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

import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.liquigraph.core.exception.ConditionExecutionException;
import org.liquigraph.core.model.AndQuery;
import org.liquigraph.core.model.OrQuery;
import org.liquigraph.core.model.Precondition;
import org.liquigraph.core.model.PreconditionErrorPolicy;
import org.liquigraph.core.model.Query;
import org.liquigraph.core.model.SimpleQuery;
import org.liquigraph.testing.JdbcAwareGraphDatabase;
import org.liquigraph.testing.ParameterizedDatabaseIT;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class ConditionExecutorIT extends ParameterizedDatabaseIT {

    private final ConditionExecutor executor = new ConditionExecutor();

    public ConditionExecutorIT(String description, JdbcAwareGraphDatabase graphDb, String uri) {
        super(description, graphDb, uri);
    }

    @Test
    public void executes_simple_precondition() {
        graphDb.rollbackNewConnection(uri, connection ->
            assertThat(executor.executeCondition(connection, simplePrecondition("RETURN true AS result"))).isTrue());
    }

    @Test
    public void executes_nested_and_precondition_queries() {
        graphDb.rollbackNewConnection(uri, connection ->
            assertThat(executor.executeCondition(connection, andPrecondition("RETURN true AS result", "RETURN false AS result"))).isFalse());
    }

    @Test
    public void executes_nested_or_precondition_queries() {
        graphDb.rollbackNewConnection(uri, connection ->
            assertThat(executor.executeCondition(connection, orPrecondition("RETURN true AS result", "RETURN false AS result"))).isTrue());
    }

    @Test
    public void executes_nested_mixed_precondition_queries_like_a_charm() {
        AndQuery andQuery = new AndQuery();
        andQuery.setQueries(Arrays.asList(
            orPreconditionQuery("RETURN false AS result", "RETURN true AS result"),
            simplePreconditionQuery("RETURN true AS result")
        ));

        graphDb.rollbackNewConnection(uri, connection ->
            assertThat(executor.executeCondition(connection, precondition(andQuery))).isTrue());
    }

    @Test
    public void fails_with_invalid_cypher_query() {
        assertThatThrownBy(() ->
            graphDb.rollbackNewConnection(uri, connection ->
                executor.executeCondition(connection, simplePrecondition("toto"))))
        .isInstanceOf(ConditionExecutionException.class)
        .hasMessageContaining("Invalid input 't'");
    }

    @Test
    public void fails_with_badly_named_precondition_result_column() {
        assertThatThrownBy(() ->
            graphDb.rollbackNewConnection(uri, connection ->
                executor.executeCondition(connection, simplePrecondition("RETURN true"))))
        .isInstanceOf(ConditionExecutionException.class)
        .hasMessageContaining("Make sure your query <RETURN true> yields exactly one column named or aliased 'result'.");
    }

    @Test
    public void fails_with_unknown_query_type() {
        Precondition precondition = new Precondition();
        precondition.setQuery(new Query() { });

        assertThatThrownBy(() ->
            graphDb.rollbackNewConnection(uri, connection -> {
                executor.executeCondition(connection, precondition);
            }))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining(String.format("Unsupported query type <%s$1>", this.getClass().getName()));
    }

    private Precondition simplePrecondition(String query) {
        return precondition(simplePreconditionQuery(query));
    }

    private Precondition andPrecondition(String firstQuery, String secondQuery) {
        return precondition(andPreconditionQuery(firstQuery, secondQuery));
    }

    private Precondition orPrecondition(String firstQuery, String secondQuery) {
        return precondition(orPreconditionQuery(firstQuery, secondQuery));
    }

    private AndQuery andPreconditionQuery(String firstQuery, String secondQuery) {
        AndQuery andQuery = new AndQuery();
        andQuery.setQueries(simpleQueries(firstQuery, secondQuery));
        return andQuery;
    }

    private OrQuery orPreconditionQuery(String firstQuery, String secondQuery) {
        OrQuery orQuery = new OrQuery();
        orQuery.setQueries(simpleQueries(firstQuery, secondQuery));
        return orQuery;
    }

    private List<Query> simpleQueries(String firstQuery, String secondQuery) {
        return Arrays.asList(
        simplePreconditionQuery(firstQuery),
        simplePreconditionQuery(secondQuery)
        );
    }

    private Precondition precondition(Query query) {
        Precondition precondition = new Precondition();
        precondition.setPolicy(PreconditionErrorPolicy.MARK_AS_EXECUTED);
        precondition.setQuery(query);
        return precondition;
    }

    private SimpleQuery simplePreconditionQuery(String query) {
        SimpleQuery simpleQuery = new SimpleQuery();
        simpleQuery.setQuery(query);
        return simpleQuery;
    }
}
