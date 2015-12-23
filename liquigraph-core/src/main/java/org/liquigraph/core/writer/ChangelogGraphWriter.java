package org.liquigraph.core.writer;

import org.liquigraph.core.exception.PreconditionNotMetException;
import org.liquigraph.core.model.Changeset;
import org.liquigraph.core.model.Precondition;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static com.google.common.base.Throwables.propagate;
import static java.lang.String.format;
import static org.liquigraph.core.writer.PreconditionResult.NO_PRECONDITION;

public class ChangelogGraphWriter implements ChangelogWriter {

    private static final String CHANGESET_UPSERT =
        "MERGE (changelog:__LiquigraphChangelog) " +
        "MERGE (changelog)<-[ewc:EXECUTED_WITHIN_CHANGELOG]-(changeset:__LiquigraphChangeset {id: {1}}) " +
        "ON MATCH SET  changeset.checksum = {2}, " +
        "              changeset.query = {3} " +
        "ON CREATE SET changeset.checksum = {2}," +
        "              changeset.query = {3}," +
        "              changeset.author = {4}," +
        "              ewc.time = timestamp()";

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
    public void write(Collection<Changeset> changelog) {
        for (Changeset changeset : changelog) {
            StatementExecution statementExecution = executeStatement(changeset);
            if (statementExecution == StatementExecution.IGNORE_FAILURE) {
                continue;
            }
            insertChangeset(connection, changeset);
        }
    }

    private StatementExecution executeStatement(Changeset changeset) {
        try (Statement statement = connection.createStatement()) {
            Precondition precondition = changeset.getPrecondition();
            PreconditionResult preconditionResult = executePrecondition(precondition);

            if (preconditionResult.executedSuccessfully()) {
                for (String query : changeset.getQueries()) {
                    statement.execute(query);
                }
            }
            else {
                switch (preconditionResult.errorPolicy()) {
                    /*
                     * ignore MARK_AS_EXECUTED:
                     * the changeset should just be inserted in the history graph
                     * without actually being executed
                     */
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
            }
            connection.commit();
        } catch (SQLException e) {
            throw propagate(e);
        }
        return StatementExecution.SUCCESS;
    }

    private PreconditionResult executePrecondition(Precondition precondition) {
        if (precondition == null) {
            return NO_PRECONDITION;
        }
        try (Statement ignored = connection.createStatement()) {
            return preconditionExecutor.executePrecondition(connection, precondition);
        } catch (SQLException e) {
            throw propagate(e);
        }
    }

    private void insertChangeset(Connection connection, Changeset changeset) {
        Map<Integer, Object> parameters = parameters(changeset);
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

    private Map<Integer, Object> parameters(Changeset changeset) {
        Collection<String> queries = changeset.getQueries();
        String checksum = changeset.getChecksum();
        Map<Integer, Object> parameters = new HashMap<>();
        parameters.put(1, changeset.getId());
        parameters.put(2, checksum);
        parameters.put(3, queries.toArray(new String[queries.size()]));
        parameters.put(4, changeset.getAuthor());
        return parameters;
    }

    private enum StatementExecution {
        SUCCESS, IGNORE_FAILURE;
    }
}
