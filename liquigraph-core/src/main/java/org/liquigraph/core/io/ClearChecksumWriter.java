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

import org.neo4j.jdbc.impl.ListArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

import static java.sql.Types.VARCHAR;
import static org.liquigraph.core.exception.Throwables.propagate;

public class ClearChecksumWriter {

    private static final String CLEAR_CHECKSUM_QUERY =
        "MATCH (changeset:__LiquigraphChangeset) " +
            "WHERE ? = true OR changeset.id IN (?) " +
            "SET  changeset.checksum = null ";
    private final Connection writeConnection;

    private static final Logger LOGGER = LoggerFactory.getLogger(ClearChecksumWriter.class);

    public ClearChecksumWriter(Connection writeConnection) {
        this.writeConnection = writeConnection;
    }

    public void write(Collection<String> changesets) {
        try (PreparedStatement statement = writeConnection.prepareStatement(CLEAR_CHECKSUM_QUERY)) {
            statement.setBoolean(1, changesets.isEmpty());
            statement.setArray(2, new ListArray(new ArrayList<>(changesets), VARCHAR));
            statement.execute();
            LOGGER.debug("Executing query: {}", CLEAR_CHECKSUM_QUERY);
            writeConnection.commit();
            LOGGER.debug("Committing transaction");
        } catch (SQLException e) {
            LOGGER.error("ClearCheckSumsWriter failed to execute", e);
            throw propagate(e);
        }
    }
}
