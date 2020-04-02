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

import org.liquigraph.core.exception.PreconditionNotMetException;
import org.liquigraph.core.model.Changeset;
import org.liquigraph.core.model.Condition;
import org.liquigraph.core.model.Postcondition;
import org.liquigraph.core.model.Precondition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.Supplier;

import static java.lang.String.format;
import static org.liquigraph.core.exception.Throwables.propagate;

public class ChangelogGraphWriter implements ChangelogWriter {

    private static final String CHANGESET_UPSERT =
        "MERGE (changelog:__LiquigraphChangelog) " +
            "MERGE (changelog)<-[ewc:EXECUTED_WITHIN_CHANGELOG]-(changeset:__LiquigraphChangeset {id: {1}, author: {3}}) " +
            "ON MATCH SET  changeset.checksum = {2} " +
            "ON CREATE SET changeset.checksum = {2}, " +
            "              ewc.time = timestamp() " +
            "WITH changeset " +
            // deletes previous stored queries, if any
            "OPTIONAL MATCH (changeset)<-[eq:EXECUTED_WITHIN_CHANGESET]-(query:__LiquigraphQuery) " +
            "DELETE eq, query " +
            "RETURN changeset ";

    private static final String QUERY_UPSERT =
        // stores the possibly updated queries
        "MATCH (changeset:__LiquigraphChangeset {id: {1}, author: {2}}) " +
            "CREATE (changeset)<-[:EXECUTED_WITHIN_CHANGESET {`order`:{3}}]-(:__LiquigraphQuery {query: {4}})";

    private static final Logger LOGGER = LoggerFactory.getLogger(ChangelogGraphWriter.class);

    private final Connection writeConnection;
    private final Supplier<Connection> connectionSupplier;
    private final ConditionExecutor conditionExecutor;

    public ChangelogGraphWriter(Connection writeConnection,
                                Supplier<Connection> connectionSupplier,
                                ConditionExecutor conditionExecutor) {
        this.writeConnection = writeConnection;
        this.connectionSupplier = connectionSupplier;
        this.conditionExecutor = conditionExecutor;
    }

    /**
     * Runs the set of migrations against the configured database and inserts them
     * in the persisted migration graph.
     * <p>
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
            insertChangeset(changeset);
        }
    }

    private StatementExecution executeStatement(Changeset changeset) {
        try {
            Precondition precondition = changeset.getPrecondition();
            if (!(precondition == null || executeCondition(precondition))) {
                LOGGER.warn("Precondition of changeset ID {} by {} failed", changeset.getId(), changeset.getAuthor());
                return handleFailedPrecondition(precondition, changeset);
            }

            boolean postConditionApplies;
            do {
                LOGGER.info("Executing postcondition of changeset ID {} by {}", changeset.getId(), changeset.getAuthor());
                executeChangesetQueries(changeset.getQueries());

                Postcondition postcondition = changeset.getPostcondition();
                postConditionApplies = postcondition != null && executeCondition(postcondition);
            } while (postConditionApplies);
        } catch (SQLException e) {
            LOGGER.error("Changeset ID {} by {} failed to execute", changeset.getId(), changeset.getAuthor(), e);
            throw propagate(e);
        }
        LOGGER.info("Changeset ID {} by {} was just executed", changeset.getId(), changeset.getAuthor());
        return StatementExecution.SUCCESS;
    }

    private void executeChangesetQueries(Collection<String> queries) throws SQLException {
        try (Statement statement = writeConnection.createStatement()) {
            for (String query : queries) {
                statement.execute(query);
                LOGGER.debug("Executing query: {}", query);
            }
            writeConnection.commit();
            LOGGER.debug("Committing transaction");
        }
    }

    private static StatementExecution handleFailedPrecondition(Precondition precondition,
                                                               Changeset changeset) {
        switch (precondition.getPolicy()) {
            case MARK_AS_EXECUTED:
                LOGGER.info("Skipping execution of changeset {} by {} but marking as executed", changeset.getId(), changeset.getAuthor());
                return StatementExecution.SUCCESS;
            case CONTINUE:
                LOGGER.info("Ignoring precondition failure of changeset {} by {}", changeset.getId(), changeset.getAuthor());
                return StatementExecution.IGNORE_FAILURE;
            case FAIL:
                LOGGER.info("Failing precondition of changeset {} by {}. Aborting now.", changeset.getId(), changeset.getAuthor());
                throw new PreconditionNotMetException(
                    format(
                        "Changeset id=<%s>, author=<%s>: precondition query %s failed with policy <%s>. Aborting.",
                        changeset.getId(),
                        changeset.getAuthor(),
                        precondition.getQuery(),
                        precondition.getPolicy()
                    )
                );
            default:
                throw new IllegalArgumentException(
                    format(
                        "Changeset id=<%s>, author=<%s>: unsupported policy <%s>. Aborting.",
                        changeset.getId(),
                        changeset.getAuthor(),
                        precondition.getPolicy()
                    )
                );
        }
    }

    private boolean executeCondition(Condition condition) {
        try (Connection readConnection = connectionSupplier.get()) {
            boolean conditionResult = conditionExecutor.executeCondition(readConnection, condition);
            readConnection.rollback(); // make sure the condition does not actually modify the data
            return conditionResult;
        } catch (SQLException e) {
            throw propagate(e);
        }
    }

    private void insertChangeset(Changeset changeset) {
        try (PreparedStatement changesetStmt = writeConnection.prepareStatement(CHANGESET_UPSERT);
             PreparedStatement queryStmt = writeConnection.prepareStatement(QUERY_UPSERT)) {

            insertChangesetNode(changeset, changesetStmt);
            insertQueryNodes(changeset, queryStmt);

            writeConnection.commit();
        } catch (SQLException e) {
            throw propagate(e);
        }
    }

    private void insertChangesetNode(Changeset changeset, PreparedStatement changesetStmt) throws SQLException {
        populateChangesetStatement(changeset, changesetStmt);
        changesetStmt.execute();
    }

    private void insertQueryNodes(Changeset changeset, PreparedStatement queryStmt) throws SQLException {
        queryStmt.setString(1, changeset.getId());
        queryStmt.setString(2, changeset.getAuthor());
        Collection<String> queries = changeset.getQueries();
        for (int i = 0; i < queries.size(); i++) {
            queryStmt.setInt(3, i);
            queryStmt.setString(4, getNth(queries, i));
            queryStmt.execute();
        }
    }

    private <T> T getNth(Collection<T> items, int index) {
        T result = null;
        int i = 0;
        for (T item : items) {
            if (i++ == index) {
                result = item;
            }
        }
        if (result == null) {
            throw new NoSuchElementException(String.format("element %d not found", index));
        }
        return result;
    }

    private void populateChangesetStatement(Changeset changeset, PreparedStatement changesetStmt) throws SQLException {
        for (Integer key : changesetParameters(changeset).keySet()) {
            changesetStmt.setObject(key, changesetParameters(changeset).get(key));
        }
    }

    private Map<Integer, Object> changesetParameters(Changeset changeset) {
        String checksum = changeset.getChecksum();
        Map<Integer, Object> parameters = new HashMap<>();
        parameters.put(1, changeset.getId());
        parameters.put(2, checksum);
        parameters.put(3, changeset.getAuthor());
        return parameters;
    }

    private enum StatementExecution {
        SUCCESS, IGNORE_FAILURE;
    }
}
