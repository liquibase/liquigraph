package com.liquigraph.core.graph;

import com.liquigraph.core.model.Changeset;
import org.neo4j.cypher.javacompat.ExecutionEngine;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.ResourceIterator;
import org.neo4j.graphdb.Transaction;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static com.google.common.collect.Iterators.getOnlyElement;

public class ChangelogWriter {

    private static final String LATEST_INDEX =
        "MERGE (changelog:__LiquigraphChangelog) " +
        "WITH changelog " +
        "OPTIONAL MATCH (changelog)<-[exec:EXECUTED_WITHIN_CHANGELOG]-(:__LiquigraphChangeset)" +
        "RETURN COALESCE(MAX(exec.order), 0) AS lastIndex";

    private static final String NEW_CHANGESET = "MATCH (changelog:__LiquigraphChangelog) " +
        "CREATE (changelog)<-[:EXECUTED_WITHIN_CHANGELOG {order: {index}}]-(:__LiquigraphChangeset {" +
        "   id: {id}," +
        "   author: {author}," +
        "   query: {query}," +
        "   checksum: {checksum}" +
        "})";

    public void write(GraphDatabaseService graphDatabase, Collection<Changeset> changelogsToInsert) {
        try (Transaction transaction = graphDatabase.beginTx()) {
            ExecutionEngine cypherEngine = new ExecutionEngine(graphDatabase);
            long index = latestPersistedIndex(cypherEngine) + 1L;

            for (Changeset changeset : changelogsToInsert) {
                cypherEngine.execute(changeset.getQuery());
                cypherEngine.execute(NEW_CHANGESET, parameters(changeset, index));
                index++;
            }

            transaction.success();
        }
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
}
