package com.liquigraph.core.writer;

import com.liquigraph.core.model.Changeset;
import org.neo4j.graphdb.Node;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;

import static com.google.common.base.Throwables.propagate;
import static com.google.common.collect.Lists.newLinkedList;

public class ChangelogReader {

    private static final String MATCH_CHANGESETS =
        "MATCH (changelog:__LiquigraphChangelog)<-[exec:EXECUTED_WITHIN_CHANGELOG]-(changeset:__LiquigraphChangeset) " +
        "RETURN changeset " +
        "ORDER BY exec.order ASC";

    public final Collection<Changeset> read(Connection connection) {
        Collection<Changeset> changesets = newLinkedList();

        try (Statement statement = connection.createStatement();
            ResultSet result = statement.executeQuery(MATCH_CHANGESETS)) {
            while (result.next()) {
                Node changeset = (Node) result.getObject("changeset");
                changesets.add(changeset(changeset));
            }
            connection.commit();
        }
        catch (SQLException e) {
            throw propagate(e);
        }
        return changesets;
    }

    private Changeset changeset(Node node) {
        Changeset changeset = new Changeset();
        changeset.setAuthor(String.valueOf(node.getProperty("author")));
        changeset.setId(String.valueOf(node.getProperty("id")));
        changeset.setQuery(String.valueOf(node.getProperty("query")));
        changeset.setChecksum(String.valueOf(node.getProperty("checksum")));
        return changeset;
    }
}
