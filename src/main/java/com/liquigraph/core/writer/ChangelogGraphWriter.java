package com.liquigraph.core.writer;

import com.liquigraph.core.exception.PreconditionNotMetException;
import com.liquigraph.core.model.Changeset;
import com.liquigraph.core.model.Precondition;
import org.neo4j.cypher.javacompat.ExecutionEngine;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.ResourceIterator;
import org.neo4j.graphdb.Transaction;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static com.google.common.collect.Iterators.getOnlyElement;
import static java.lang.String.format;

public class ChangelogGraphWriter implements ChangelogWriter {

    private static final String LATEST_INDEX =
        "MERGE (changelog:__LiquigraphChangelog) " +
            "WITH changelog " +
            "OPTIONAL MATCH (changelog)<-[exec:EXECUTED_WITHIN_CHANGELOG]-(:__LiquigraphChangeset)" +
            "RETURN COALESCE(MAX(exec.order), 0) AS lastIndex";

    private static final String CHANGESET_UPSERT =
        "MATCH (changelog:__LiquigraphChangelog) " +
            "MERGE (changelog)<-[:EXECUTED_WITHIN_CHANGELOG {order: {index}}]-(changeset:__LiquigraphChangeset {id: {id}}) " +
            "ON MATCH SET  changeset.checksum = {checksum}, " +
            "              changeset.query = {query}" +
            "ON CREATE SET changeset.author = {author}, " +
            "              changeset.query = {query}, " +
            "              changeset.checksum = {checksum}";

    private final GraphDatabaseService graphDatabase;
    private final PreconditionExecutor preconditionExecutor;

    public ChangelogGraphWriter(GraphDatabaseService graphDatabase, PreconditionExecutor preconditionExecutor) {
        this.graphDatabase = graphDatabase;
        this.preconditionExecutor = preconditionExecutor;
    }

    @Override
    public void write(Collection<Changeset> changelogsToInsert) {
        ExecutionEngine cypherEngine = new ExecutionEngine(graphDatabase);
        long index = latestPersistedIndex(graphDatabase) + 1L;

        for (Changeset changeset : changelogsToInsert) {
            try (Transaction transaction = graphDatabase.beginTx()) {
                Precondition precondition = changeset.getPrecondition();
                PreconditionResult result = preconditionExecutor.executePrecondition(cypherEngine, precondition).orNull();
                if (result == null || result.executedSuccessfully()) {
                    cypherEngine.execute(changeset.getQuery());
                    upsertChangeset(cypherEngine, index, changeset);
                }
                else {
                    switch (result.errorPolicy()) {
                        case CONTINUE:
                            continue;
                        case MARK_AS_EXECUTED:
                            upsertChangeset(cypherEngine, index, changeset);
                            break;
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
                transaction.success();
            }
            index++;
        }
    }

    private void upsertChangeset(ExecutionEngine cypherEngine, long index, Changeset changeset) {
        cypherEngine.execute(CHANGESET_UPSERT, parameters(changeset, index));
    }

    private long latestPersistedIndex(GraphDatabaseService graphDb) {
        try (Transaction transaction = graphDb.beginTx();
             ResourceIterator<Long> results = new ExecutionEngine(graphDb).execute(LATEST_INDEX).columnAs("lastIndex")) {

            Long result = getOnlyElement(results);
            transaction.success();
            return result;
        }
    }

    private Map<String, Object> parameters(Changeset changeset, long index) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("index", index);
        parameters.put("id", changeset.getId());
        parameters.put("author", changeset.getAuthor());
        parameters.put("query", changeset.getQuery());
        parameters.put("checksum", changeset.getChecksum());
        return parameters;
    }
}
