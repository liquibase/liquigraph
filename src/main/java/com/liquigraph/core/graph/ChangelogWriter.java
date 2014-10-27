package com.liquigraph.core.graph;

import com.google.common.base.Optional;
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

import static com.google.common.base.Optional.absent;
import static com.google.common.collect.Iterators.getOnlyElement;
import static java.lang.String.format;

public class ChangelogWriter {

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

    public void write(GraphDatabaseService graphDatabase, Collection<Changeset> changelogsToInsert) {
        try (Transaction transaction = graphDatabase.beginTx()) {
            ExecutionEngine cypherEngine = new ExecutionEngine(graphDatabase);
            long index = latestPersistedIndex(cypherEngine) + 1L;

            for (Changeset changeset : changelogsToInsert) {
                Precondition precondition = changeset.getPrecondition();
                PreconditionResult result = executePrecondition(cypherEngine, precondition).orNull();
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
                                    "Changeset <%s>: precondition query <%s> failed with policy <%s>. Aborting.",
                                    changeset.getId(),
                                    precondition.getQuery(),
                                    precondition.getPolicy()
                                )
                            );
                    }
                }
                index++;
            }

            transaction.success();
        }
    }

    private void upsertChangeset(ExecutionEngine cypherEngine, long index, Changeset changeset) {
        cypherEngine.execute(CHANGESET_UPSERT, parameters(changeset, index));
    }

    private long latestPersistedIndex(ExecutionEngine cypherEngine) {
        try (ResourceIterator<Long> results = cypherEngine.execute(LATEST_INDEX).columnAs("lastIndex")) {
            return getOnlyElement(results);
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

    private Optional<PreconditionResult> executePrecondition(ExecutionEngine cypherEngine, Precondition precondition) {
        if (precondition == null) {
            return absent();
        }
        return Optional.of(applyPrecondition(cypherEngine, precondition));
    }

    private PreconditionResult applyPrecondition(ExecutionEngine cypherEngine, Precondition precondition) {
        try (ResourceIterator<Boolean> results = cypherEngine.execute(precondition.getQuery()).columnAs("result")) {
            return new PreconditionResult(precondition.getPolicy(), getOnlyElement(results));
        }
    }
}
