package com.liquigraph.core.writer;

import com.liquigraph.core.model.Changeset;
import org.neo4j.cypher.javacompat.ExecutionEngine;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.ResourceIterator;
import org.neo4j.graphdb.Transaction;

import java.util.Collection;
import java.util.Map;

import static com.google.common.collect.Lists.newLinkedList;

public class ChangelogReader {

    private static final String MATCH_CHANGESETS =
        "MATCH (changelog:__LiquigraphChangelog)<-[exec:EXECUTED_WITHIN_CHANGELOG]-(changeset:__LiquigraphChangeset) " +
        "RETURN changeset " +
        "ORDER BY exec.order ASC";

    public final Collection<Changeset> read(GraphDatabaseService graphDatabase) {
        Collection<Changeset> changesets = newLinkedList();
        ExecutionEngine engine = new ExecutionEngine(graphDatabase);
        try (Transaction transaction = graphDatabase.beginTx();
            ResourceIterator<Map<String, Object>> result = engine.execute(MATCH_CHANGESETS).iterator()) {
            while(result.hasNext()) {
                Node node = (Node) result.next().get("changeset");
                changesets.add(changeset(node));
            }
            transaction.success();
        }
        return changesets;
    }

    private Changeset changeset(Node changesetAttributes) {
        Changeset changeset = new Changeset();
        changeset.setAuthor(String.valueOf(changesetAttributes.getProperty("author")));
        changeset.setId(String.valueOf(changesetAttributes.getProperty("id")));
        changeset.setQuery(String.valueOf(changesetAttributes.getProperty("query")));
        changeset.setChecksum(String.valueOf(changesetAttributes.getProperty("checksum")));
        return changeset;
    }
}
