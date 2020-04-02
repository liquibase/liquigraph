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

import org.liquigraph.core.model.Changeset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import static java.lang.String.format;
import static java.util.Collections.unmodifiableCollection;
import static org.liquigraph.core.exception.Throwables.propagate;

public class ChangelogGraphReader {

    private static final String MIGRATE_PRE_1_0_RC3_CHANGELOG =
        "MATCH (:__LiquigraphChangelog)<-[exec:EXECUTED_WITHIN_CHANGELOG]-(changeset:__LiquigraphChangeset) " +
            "WHERE EXISTS(exec.`order`) AND EXISTS(changeset.query) " +
            "SET exec.time = exec.`order` " +
            "WITH exec, changeset " +
            "REMOVE exec.`order` " +
            "WITH changeset " +
            "CREATE (changeset)<-[:EXECUTED_WITHIN_CHANGESET {`order`: 0}]-(query:__LiquigraphQuery) " +
            "SET query.query = changeset.query " +
            "WITH changeset " +
            "REMOVE changeset.query";

    private static final String MATCH_CHANGESETS =
        "MATCH (:__LiquigraphChangelog)<-[changelog_execution:EXECUTED_WITHIN_CHANGELOG]-(changeset:__LiquigraphChangeset), " +
        "      (changeset)<-[changeset_execution:EXECUTED_WITHIN_CHANGESET]-(query:__LiquigraphQuery) " +
        "WITH changelog_execution.`time` AS changeset_position, changeset, query " +
        "ORDER BY changeset_position ASC, changeset_execution.`order` ASC " +
        "WITH changeset_position, changeset, COLLECT(query.query) AS queries " +
        "RETURN {" +
        "   id: changeset.id, " +
        "   author:changeset.author, " +
        "   checksum:changeset.checksum, " +
        "   query:queries" +
        "} AS changeset " +
        "ORDER BY changeset_position ASC";

    private static final Logger LOGGER = LoggerFactory.getLogger(ChangelogFileWriter.class);

    public final Collection<Changeset> read(Connection connection) {
        Collection<Changeset> changesets = new ArrayList<>();
        try (Statement statement = connection.createStatement()) {
            LOGGER.debug("Migrating pre 1.0-RC3 history graph");
            statement.execute(MIGRATE_PRE_1_0_RC3_CHANGELOG);
            try (ResultSet result = statement.executeQuery(MATCH_CHANGESETS)) {
                while (result.next()) {
                    changesets.add(mapRow(result.getObject("changeset")));
                }
            }
            connection.commit();
            LOGGER.debug("Retrieved {} changesets", changesets.size());
            return changesets;
        } catch (SQLException e) {
            throw propagate(e);
        }
    }

    @SuppressWarnings("unchecked")
    private Changeset mapRow(Object line) throws SQLException {
        if (line instanceof Map) {
            return changeset((Map<String, Object>) line);
        }
        throw new IllegalArgumentException(format(
           "Unsupported row.\n\t" +
           "Cannot parse: %s", line));
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
        return unmodifiableCollection((Collection<String>) rawQuery);
    }
}
