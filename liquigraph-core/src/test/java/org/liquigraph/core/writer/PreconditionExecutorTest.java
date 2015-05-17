package org.liquigraph.core.writer;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.liquigraph.core.exception.PreconditionException;
import org.liquigraph.core.model.*;
import org.liquigraph.core.rules.EmbeddedGraphDatabaseRule;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;

public class PreconditionExecutorTest {

    @Rule
    public EmbeddedGraphDatabaseRule graphDatabaseRule = new EmbeddedGraphDatabaseRule("neotest");

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private PreconditionExecutor executor = new PreconditionExecutor();

    @Test
    public void returns_no_result_when_precondition_is_null() {
        Optional<PreconditionResult> result = executor.executePrecondition(graphDatabaseRule.jdbcConnection(), null);

        assertThat(result).isEqualTo(Optional.absent());
    }

    @Test
    public void executes_simple_precondition() throws SQLException {
        Connection connection = graphDatabaseRule.jdbcConnection();
        try (Statement ignored = connection.createStatement()) {
            Optional<PreconditionResult> maybeResult = executor.executePrecondition(
                connection,
                simplePrecondition(PreconditionErrorPolicy.MARK_AS_EXECUTED, "RETURN true AS result")
            );

            PreconditionResult result = maybeResult.get();
            assertThat(result.errorPolicy()).isEqualTo(PreconditionErrorPolicy.MARK_AS_EXECUTED);
            assertThat(result.executedSuccessfully()).isTrue();
        }
    }

    @Test
    public void executes_nested_and_precondition_queries() throws SQLException {
        Connection connection = graphDatabaseRule.jdbcConnection();
        try (Statement ignored = connection.createStatement()) {
            Optional<PreconditionResult> maybeResult = executor.executePrecondition(
                connection,
                andPrecondition(PreconditionErrorPolicy.FAIL, "RETURN true AS result", "RETURN false AS result")
            );

            PreconditionResult result = maybeResult.get();
            assertThat(result.errorPolicy()).isEqualTo(PreconditionErrorPolicy.FAIL);
            assertThat(result.executedSuccessfully()).isFalse();
        }
    }

    @Test
    public void executes_nested_or_precondition_queries() throws SQLException {
        Connection connection = graphDatabaseRule.jdbcConnection();
        try (Statement ignored = connection.createStatement()) {
            Optional<PreconditionResult> maybeResult = executor.executePrecondition(
                connection,
                orPrecondition(PreconditionErrorPolicy.CONTINUE, "RETURN true AS result", "RETURN false AS result")
            );

            PreconditionResult result = maybeResult.get();
            assertThat(result.errorPolicy()).isEqualTo(PreconditionErrorPolicy.CONTINUE);
            assertThat(result.executedSuccessfully()).isTrue();
        }
    }

    @Test
    public void executes_nested_mixed_precondition_queries_like_a_charm() throws SQLException {
        Precondition precondition = precondition(PreconditionErrorPolicy.MARK_AS_EXECUTED);
        AndQuery andQuery = new AndQuery();
        andQuery.setPreconditionQueries(newArrayList(
            orPreconditionQuery("RETURN false AS result", "RETURN true AS result"),
            simplePreconditionQuery("RETURN true AS result")
        ));
        precondition.setQuery(andQuery);

        Connection connection = graphDatabaseRule.jdbcConnection();
        try (Statement ignored = connection.createStatement()) {
            Optional<PreconditionResult> maybeResult = executor.executePrecondition(
                connection,
                precondition
            );

            PreconditionResult result = maybeResult.get();
            assertThat(result.errorPolicy()).isEqualTo(PreconditionErrorPolicy.MARK_AS_EXECUTED);
            assertThat(result.executedSuccessfully()).isTrue();
        }
    }

    @Test
    public void fails_with_invalid_cypher_query() throws SQLException {
        thrown.expect(PreconditionException.class);
        thrown.expectMessage("\tQuery <toto> should yield exactly one column named or aliased 'result'.\n" +
            "\tCause: Error executing query toto\n" +
            " with params {}");

        Connection connection = graphDatabaseRule.jdbcConnection();
        try (Statement ignored = connection.createStatement()) {
            executor.executePrecondition(
                connection,
                simplePrecondition(PreconditionErrorPolicy.MARK_AS_EXECUTED, "toto")
            );
        }
    }

    @Test
    public void fails_with_badly_named_precondition_result_column() throws SQLException {
        thrown.expect(PreconditionException.class);
        thrown.expectMessage("Query <RETURN true> should yield exactly one column named or aliased 'result'.");

        Connection connection = graphDatabaseRule.jdbcConnection();
        try (Statement ignored = connection.createStatement()) {
            executor.executePrecondition(
                connection,
                simplePrecondition(PreconditionErrorPolicy.MARK_AS_EXECUTED, "RETURN true")
            );
        }
    }

    @Test
    public void fails_with_unknown_query_type() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Unsupported query type <org.liquigraph.core.writer.PreconditionExecutorTest$1>");

        Precondition precondition = new Precondition();
        precondition.setQuery(new PreconditionQuery() {});
        executor.executePrecondition(graphDatabaseRule.jdbcConnection(), precondition);
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