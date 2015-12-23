package org.liquigraph.core.writer;

import org.liquigraph.core.model.Changeset;
import org.neo4j.graphdb.Node;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

import static com.google.common.base.Throwables.propagate;
import static com.google.common.collect.Lists.newLinkedList;
import static java.lang.String.format;
import static java.util.Collections.singletonList;

public class ChangelogGraphReader {

    private static final String MATCH_CHANGESETS =
        "MATCH (changelog:__LiquigraphChangelog)<-[exec:EXECUTED_WITHIN_CHANGELOG]-(changeset:__LiquigraphChangeset) " +
        "RETURN changeset " +
        "ORDER BY exec.time ASC";

    public final Collection<Changeset> read(Connection connection) {
        Collection<Changeset> changesets = newLinkedList();
        try (Statement statement = connection.createStatement();
            ResultSet result = statement.executeQuery(MATCH_CHANGESETS)) {
            while (result.next()) {
                changesets.add(mapLine(result.getObject("changeset")));
            }
            connection.commit();
        }
        catch (SQLException e) {
            throw propagate(e);
        }
        return changesets;
    }

    @SuppressWarnings("unchecked")
    private Changeset mapLine(Object line) throws SQLException {
        if (line instanceof Node) {
            return changeset((Node) line);
        }
        if (line instanceof Map) {
            return changeset((Map<String, Object>) line);
        }
        throw new IllegalStateException(format("Unrecognized result type:%s", line.getClass().getName()));
    }

    private Changeset changeset(Node node) {
        Changeset changeset = new Changeset();
        changeset.setAuthor(String.valueOf(node.getProperty("author")));
        changeset.setId(String.valueOf(node.getProperty("id")));
        changeset.setQueries(adaptQueries(node.getProperty("query")));
        changeset.setChecksum(String.valueOf(node.getProperty("checksum")));
        return changeset;
    }

    private Changeset changeset(Map<String, Object> node) {
        Changeset changeset = new Changeset();
        changeset.setAuthor(String.valueOf(node.get("author")));
        changeset.setId(String.valueOf(node.get("id")));
        changeset.setQueries(adaptQueries(node.get("query")));
        changeset.setChecksum(String.valueOf(node.get("checksum")));
        return changeset;
    }

    private Collection<String> adaptQueries(Object rawQuery) {
        if (rawQuery instanceof String) {
            return singletonList((String) rawQuery);
        }
        if (rawQuery instanceof String[]) {
            return Arrays.asList((String[]) rawQuery);
        }
        throw new IllegalArgumentException(
           "Unsupported query format.\n\t" +
              "Should be a String or a String[].\n\t" +
              "Found value: " + rawQuery
        );
    }
}
