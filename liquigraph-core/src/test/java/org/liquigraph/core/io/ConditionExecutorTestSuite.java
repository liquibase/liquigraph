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
package org.liquigraph.core.io;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.liquigraph.core.GraphIntegrationTestSuite;
import org.liquigraph.core.exception.ConditionExecutionException;
import org.liquigraph.core.model.AndQuery;
import org.liquigraph.core.model.OrQuery;
import org.liquigraph.core.model.Precondition;
import org.liquigraph.core.model.PreconditionErrorPolicy;
import org.liquigraph.core.model.Query;
import org.liquigraph.core.model.SimpleQuery;
import org.slf4j.bridge.SLF4JBridgeHandler;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.liquigraph.core.io.PatternMatcher.matchesPattern;

public abstract class ConditionExecutorTestSuite implements GraphIntegrationTestSuite {

    static {
        SLF4JBridgeHandler.removeHandlersForRootLogger();
    }

    private ConditionExecutor executor = new ConditionExecutor();

    @Test
    public void executes_simple_precondition() throws SQLException {
        try (Connection connection = graphDatabase().newConnection()) {
            try (Statement ignored = connection.createStatement()) {
                boolean result = executor.executeCondition(
                    connection,
                    simplePrecondition("RETURN true AS result")
                );

                assertThat(result).isTrue();
            }
        }
    }

    @Test
    public void executes_nested_and_precondition_queries() throws SQLException {
        try (Connection connection = graphDatabase().newConnection()) {
            try (Statement ignored = connection.createStatement()) {
                boolean result = executor.executeCondition(
                    connection,
                    andPrecondition("RETURN true AS result", "RETURN false AS result")
                );

                assertThat(result).isFalse();
            }
        }
    }

    @Test
    public void executes_nested_or_precondition_queries() throws SQLException {
        try (Connection connection = graphDatabase().newConnection()) {
            try (Statement ignored = connection.createStatement()) {
                boolean result = executor.executeCondition(
                    connection,
                    orPrecondition("RETURN true AS result", "RETURN false AS result")
                );

                assertThat(result).isTrue();
            }
        }
    }

    @Test
    public void executes_nested_mixed_precondition_queries_like_a_charm() throws SQLException {
        AndQuery andQuery = new AndQuery();
        andQuery.setQueries(Arrays.asList(
            orPreconditionQuery("RETURN false AS result", "RETURN true AS result"),
            simplePreconditionQuery("RETURN true AS result")
        ));
        Precondition precondition = precondition(andQuery);

        try (Connection connection = graphDatabase().newConnection()) {
            try (Statement ignored = connection.createStatement()) {
                boolean result = executor.executeCondition(
                    connection,
                    precondition
                );

                assertThat(result).isTrue();
            }
        }
    }

    @Test
    public void fails_with_invalid_cypher_query() {
        assertThatThrownBy(() -> {
            try (Connection connection = graphDatabase().newConnection()) {
                try (Statement ignored = connection.createStatement()) {
                    executor.executeCondition(connection, simplePrecondition("toto"));
                }
            }
        })
        .isInstanceOf(ConditionExecutionException.class)
        .hasMessageContaining("Invalid input 't'");
    }

    @Test
    public void fails_with_badly_named_precondition_result_column() {
        assertThatThrownBy(() -> {
            try (Connection connection = graphDatabase().newConnection()) {
                try (Statement ignored = connection.createStatement()) {
                    executor.executeCondition(connection, simplePrecondition("RETURN true"));
                }
            }
        })
        .isInstanceOf(ConditionExecutionException.class)
        .hasMessageContaining("Make sure your query <RETURN true> yields exactly one column named or aliased 'result'.");
    }

    @Test
    public void fails_with_unknown_query_type() {
        Precondition precondition = new Precondition();
        precondition.setQuery(new Query() {});

        assertThatThrownBy(() -> {
            try (Connection connection = graphDatabase().newConnection()) {
                executor.executeCondition(connection, precondition);
            }
        })
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Unsupported query type <org.liquigraph.core.io.ConditionExecutorTestSuite$1>");
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
