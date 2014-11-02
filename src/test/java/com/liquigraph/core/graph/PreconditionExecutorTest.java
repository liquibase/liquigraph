package com.liquigraph.core.graph;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.liquigraph.core.exception.PreconditionSyntaxException;
import com.liquigraph.core.model.*;
import com.liquigraph.core.rules.EmbeddedGraphDatabaseRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.neo4j.graphdb.Transaction;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;

public class PreconditionExecutorTest {

    @Rule
    public EmbeddedGraphDatabaseRule graphDatabaseRule = new EmbeddedGraphDatabaseRule();

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private PreconditionExecutor executor = new PreconditionExecutor();

    @Test
    public void returns_no_result_when_precondition_is_null() {
        Optional<PreconditionResult> result = executor.executePrecondition(graphDatabaseRule.cypherEngine(), null);

        assertThat(result).isEqualTo(Optional.absent());
    }

    @Test
    public void fails_with_invalid_cypher_query() {
        thrown.expect(PreconditionSyntaxException.class);
        thrown.expectMessage("\tQuery <toto> is invalid. Please check again its syntax.\n" +
            "\tMore details:\n" +
            "Invalid input 't': expected SingleStatement (line 1, column 1)\n" +
            "\"toto\"\n" +
            " ^");

        try (Transaction transaction = graphDatabaseRule.graphDatabase().beginTx()) {
            executor.executePrecondition(
                graphDatabaseRule.cypherEngine(),
                simplePrecondition(PreconditionErrorPolicy.MARK_AS_EXECUTED, "toto")
            );
        }
    }

    @Test
    public void fails_with_badly_named_precondition_result_column() {
        thrown.expect(PreconditionSyntaxException.class);
        thrown.expectMessage("Query <RETURN true> should yield exactly one column named or aliased 'result'.");

        try (Transaction transaction = graphDatabaseRule.graphDatabase().beginTx()) {
            executor.executePrecondition(
                graphDatabaseRule.cypherEngine(),
                simplePrecondition(PreconditionErrorPolicy.MARK_AS_EXECUTED, "RETURN true")
            );
        }
    }

    @Test
    public void executes_simple_precondition() {
        try (Transaction transaction = graphDatabaseRule.graphDatabase().beginTx()) {
            Optional<PreconditionResult> maybeResult = executor.executePrecondition(
                graphDatabaseRule.cypherEngine(),
                simplePrecondition(PreconditionErrorPolicy.MARK_AS_EXECUTED, "RETURN true AS result")
            );

            PreconditionResult result = maybeResult.get();
            assertThat(result.errorPolicy()).isEqualTo(PreconditionErrorPolicy.MARK_AS_EXECUTED);
            assertThat(result.executedSuccessfully()).isTrue();
            transaction.success();
        }
    }

    @Test
    public void executes_nested_and_precondition_queries() {
        try (Transaction transaction = graphDatabaseRule.graphDatabase().beginTx()) {
            Optional<PreconditionResult> maybeResult = executor.executePrecondition(
                graphDatabaseRule.cypherEngine(),
                andPrecondition(PreconditionErrorPolicy.FAIL, "RETURN true AS result", "RETURN false AS result")
            );

            PreconditionResult result = maybeResult.get();
            assertThat(result.errorPolicy()).isEqualTo(PreconditionErrorPolicy.FAIL);
            assertThat(result.executedSuccessfully()).isFalse();
            transaction.success();
        }
    }

    @Test
    public void executes_nested_or_precondition_queries() {
        try (Transaction transaction = graphDatabaseRule.graphDatabase().beginTx()) {
            Optional<PreconditionResult> maybeResult = executor.executePrecondition(
                graphDatabaseRule.cypherEngine(),
                orPrecondition(PreconditionErrorPolicy.CONTINUE, "RETURN true AS result", "RETURN false AS result")
            );

            PreconditionResult result = maybeResult.get();
            assertThat(result.errorPolicy()).isEqualTo(PreconditionErrorPolicy.CONTINUE);
            assertThat(result.executedSuccessfully()).isTrue();
            transaction.success();
        }
    }

    @Test
    public void executes_nested_mixed_precondition_queries_like_a_charm() {
        Precondition precondition = precondition(PreconditionErrorPolicy.MARK_AS_EXECUTED);
        AndQuery andQuery = new AndQuery();
        andQuery.setPreconditionQueries(newArrayList(
            orPreconditionQuery("RETURN false AS result", "RETURN true AS result"),
            simplePreconditionQuery("RETURN true AS result")
        ));
        precondition.setQuery(andQuery);

        try (Transaction transaction = graphDatabaseRule.graphDatabase().beginTx()) {
            Optional<PreconditionResult> maybeResult = executor.executePrecondition(
                graphDatabaseRule.cypherEngine(),
                precondition
            );

            PreconditionResult result = maybeResult.get();
            assertThat(result.errorPolicy()).isEqualTo(PreconditionErrorPolicy.MARK_AS_EXECUTED);
            assertThat(result.executedSuccessfully()).isTrue();
            transaction.success();
        }
    }

    private Precondition simplePrecondition(PreconditionErrorPolicy fail, String query) {
        SimpleQuery simpleQuery = simplePreconditionQuery(query);
        Precondition precondition = precondition(fail);
        precondition.setQuery(simpleQuery);
        return precondition;
    }

    private Precondition andPrecondition(PreconditionErrorPolicy errorPolicy, String firstQuery, String secondQuery) {
        Precondition precondition = precondition(errorPolicy);
        precondition.setQuery(andPreconditionQuery(firstQuery, secondQuery));
        return precondition;
    }

    private Precondition orPrecondition(PreconditionErrorPolicy errorPolicy, String firstQuery, String secondQuery) {
        Precondition precondition = precondition(errorPolicy);
        precondition.setQuery(orPreconditionQuery(firstQuery, secondQuery));
        return precondition;
    }

    private AndQuery andPreconditionQuery(String firstQuery, String secondQuery) {
        AndQuery andQuery = new AndQuery();
        andQuery.setPreconditionQueries(simpleQueries(firstQuery, secondQuery));
        return andQuery;
    }

    private OrQuery orPreconditionQuery(String firstQuery, String secondQuery) {
        OrQuery orQuery = new OrQuery();
        orQuery.setPreconditionQueries(simpleQueries(firstQuery, secondQuery));
        return orQuery;
    }

    private List<PreconditionQuery> simpleQueries(String firstQuery, String secondQuery) {
        return Lists.<PreconditionQuery>newArrayList(
            simplePreconditionQuery(firstQuery), simplePreconditionQuery(secondQuery)
        );
    }

    private Precondition precondition(PreconditionErrorPolicy policy) {
        Precondition precondition = new Precondition();
        precondition.setPolicy(policy);
        return precondition;
    }

    private SimpleQuery simplePreconditionQuery(String query) {
        SimpleQuery simpleQuery = new SimpleQuery();
        simpleQuery.setQuery(query);
        return simpleQuery;
    }
}