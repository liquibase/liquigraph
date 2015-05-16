package org.liquigraph.core.writer;

import org.liquigraph.core.exception.PreconditionNotMetException;
import org.liquigraph.core.model.Changeset;
import org.liquigraph.core.model.Precondition;
import org.liquigraph.core.model.PreconditionErrorPolicy;

import java.sql.*;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static com.google.common.base.Throwables.propagate;
import static java.lang.String.format;
import static org.liquigraph.core.model.PreconditionErrorPolicy.MARK_AS_EXECUTED;

public class ChangelogGraphWriter implements ChangelogWriter {

    private static final String LATEST_INDEX =
        "MERGE (changelog:__LiquigraphChangelog) " +
        "WITH changelog " +
        "OPTIONAL MATCH (changelog)<-[exec:EXECUTED_WITHIN_CHANGELOG]-(:__LiquigraphChangeset)" +
        "RETURN COALESCE(MAX(exec.order), 0) AS lastIndex";

    private static final String CHANGESET_UPSERT =
        "MATCH (changelog:__LiquigraphChangelog) " +
        "MERGE (changelog)<-[:EXECUTED_WITHIN_CHANGELOG {order: {1}}]-(changeset:__LiquigraphChangeset {id: {2}}) " +
        "ON MATCH SET  changeset.checksum = {3}, " +
        "              changeset.query = {4} " +
        "ON CREATE SET changeset.author = {5}, " +
        "              changeset.query = {4}, " +
        "              changeset.checksum = {3}";

    private final Connection connection;
    private final PreconditionExecutor preconditionExecutor;

    public ChangelogGraphWriter(Connection connection, PreconditionExecutor preconditionExecutor) {
        this.connection = connection;
        this.preconditionExecutor = preconditionExecutor;
    }

    /**
     * Runs the set of migrations against the configured database and inserts them
     * in the persisted migration graph.
     *
     * Please note that these two operations are performed in two separate transactions,
     * as user-defined migrations may operate on indices and those need be run apart
     * from data changes.
     */
    @Override
    public void write(Collection<Changeset> changelogsToInsert) {
        long index = latestPersistedIndex(connection) + 1L;

        for (Changeset changeset : changelogsToInsert) {
            StatementExecution statementExecution = executeStatement(changeset);
            if (statementExecution == StatementExecution.IGNORE_FAILURE) {
                continue;
            }

            insertChangeset(connection, index++, changeset);
        }
    }

    private StatementExecution executeStatement(Changeset changeset) {
        try (Statement statement = connection.createStatement()) {
            Precondition precondition = changeset.getPrecondition();
            PreconditionResult preconditionResult = executePrecondition(precondition);
            if (preconditionResult != null && !preconditionResult.executedSuccessfully()) {
                switch (preconditionResult.errorPolicy()) {
                    case CONTINUE:
                        return StatementExecution.IGNORE_FAILURE;
                    case FAIL:
                        throw new PreconditionNotMetException(
                            format(
                                "Changeset <%s>: precondition query %s failed with policy <%s>. Aborting.",
                                changeset.getId(),
                                precondition.getQuery(),
                                precondition.getPolicy()
                            )
                        );
                }
            } else if (preconditionResult == null || preconditionResult.executedSuccessfully()
                                                  || preconditionResult.errorPolicy() == MARK_AS_EXECUTED) {
                statement.execute(changeset.getQuery());
            }
            connection.commit();
        } catch (SQLException e) {
            throw propagate(e);
        }
        return StatementExecution.SUCCESS;
    }

    private PreconditionResult executePrecondition(Precondition precondition) {
        PreconditionResult result;
        try (Statement ignored = connection.createStatement()) {
            result = preconditionExecutor.executePrecondition(connection, precondition).orNull();
        } catch (SQLException e) {
            throw propagate(e);
        }
        return result;
    }

    private void insertChangeset(Connection connection, long index, Changeset changeset) {
        Map<Integer, Object> parameters = parameters(changeset, index);
        try (PreparedStatement statement = connection.prepareStatement(CHANGESET_UPSERT)) {
            for (Integer key : parameters.keySet()) {
                statement.setObject(key, parameters.get(key));
            }
            statement.execute();
            connection.commit();
        } catch (SQLException e) {
            throw propagate(e);
        }
    }

    private long latestPersistedIndex(Connection connection) {
        try (Statement statement = connection.createStatement()) {
            ResultSet resultSet = statement.executeQuery(LATEST_INDEX);
            resultSet.next();
            long lastIndex = resultSet.getLong("lastIndex");

            connection.commit();
            return lastIndex;
        } catch (SQLException e) {
            throw propagate(e);
        }
    }

    private Map<Integer, Object> parameters(Changeset changeset, long index) {
        String query = changeset.getQuery();
        String checksum = changeset.getChecksum();
        Map<Integer, Object> parameters = new HashMap<>();
        parameters.put(1, index);
        parameters.put(2, changeset.getId());
        parameters.put(3, checksum);
        parameters.put(4, query);
        parameters.put(5, changeset.getAuthor());
        return parameters;
    }

    private static enum StatementExecution {
        SUCCESS, IGNORE_FAILURE;
    }
}
