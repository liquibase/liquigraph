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
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collection;

import static org.liquigraph.core.exception.Throwables.propagate;

public class ReplaceChecksumWriter {

    private static final String REPLACE_CHECKSUM_UPDATE =
        "MATCH (changeset:__LiquigraphChangeset {id: ?, author: ?}) " +
            "SET  changeset.checksum = ?";
    private final Connection writeConnection;

    private static final Logger LOGGER = LoggerFactory.getLogger(ReplaceChecksumWriter.class);

    public ReplaceChecksumWriter(Connection writeConnection) {
        this.writeConnection = writeConnection;
    }

    public void write(Collection<Changeset> changelog) {
        for (Changeset changeset : changelog) {
            executeStatement(changeset);
        }
    }

    private void executeStatement(Changeset changeset) {
        try (PreparedStatement statement = writeConnection.prepareStatement(REPLACE_CHECKSUM_UPDATE)) {
            statement.setObject(1, changeset.getId());
            statement.setObject(2, changeset.getAuthor());
            statement.setObject(3, changeset.getChecksum());
            statement.execute();
            LOGGER.debug("Executing query: {}", REPLACE_CHECKSUM_UPDATE);
            writeConnection.commit();
            LOGGER.debug("Committing transaction");
        } catch (SQLException e) {
            LOGGER.error("Replace checksum failed for Changeset ID {} by {}", changeset.getId(), changeset.getAuthor(), e);
            throw propagate(e);
        }
        LOGGER.info("Checksum ID {} by {} was just replaced", changeset.getId(), changeset.getAuthor());
    }
}
