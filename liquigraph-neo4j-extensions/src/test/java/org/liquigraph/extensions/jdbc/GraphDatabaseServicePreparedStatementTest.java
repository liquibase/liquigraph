package org.liquigraph.extensions.jdbc;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.harness.junit.Neo4jRule;
import org.slf4j.bridge.SLF4JBridgeHandler;

import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class GraphDatabaseServicePreparedStatementTest {

    static {
        SLF4JBridgeHandler.removeHandlersForRootLogger();
    }


    @Rule
    public Neo4jRule neo4jRule = new Neo4jRule();

    private GraphDatabaseService graphDatabaseService;

    @Before
    public void prepare() {
        graphDatabaseService = neo4jRule.getGraphDatabaseService();
    }

    @Test
    public void fails_setting_out_of_bound_parameter() throws SQLException {
        try (GraphDatabaseServicePreparedStatement statement = new GraphDatabaseServicePreparedStatement(graphDatabaseService, "MATCH (lock:__LiquigraphLock {uuid:{1}, name:{2}}) DELETE lock")) {

            assertThatThrownBy(() -> statement.setString(3, "some-uuid-not-really-tho"))
              .isInstanceOf(SQLException.class)
              .hasMessage("cannot set parameter 3, only 2 parameter(s) in statement");
        }
    }

    @Test
    public void executes_successfully() throws SQLException {
        try (GraphDatabaseServicePreparedStatement statement = new GraphDatabaseServicePreparedStatement(graphDatabaseService, "MATCH (lock:__LiquigraphLock {uuid:{1}}) DELETE lock")) {

            statement.setString(1, "not-really");
            assertThatCode(statement::execute).doesNotThrowAnyException();
        }
    }

    @Test
    public void fails_to_execute_if_statement_is_closed() throws SQLException {
        GraphDatabaseServicePreparedStatement statement = new GraphDatabaseServicePreparedStatement(graphDatabaseService, "MATCH (lock:__LiquigraphLock {uuid:{1}}) DELETE lock");
        statement.close();

        assertThatThrownBy(statement::execute)
          .isInstanceOf(SQLException.class)
          .hasMessage("Prepared statement is closed");
    }

    @Test
    public void fails_to_set_string_if_statement_is_closed() throws SQLException {
        GraphDatabaseServicePreparedStatement statement = new GraphDatabaseServicePreparedStatement(graphDatabaseService, "MATCH (lock:__LiquigraphLock {uuid:{1}}) DELETE lock");
        statement.close();

        assertThatThrownBy(() -> statement.setString(1, "foobar"))
          .isInstanceOf(SQLException.class)
          .hasMessage("Prepared statement is closed");
    }
}
