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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.liquigraph.core.GraphIntegrationTestSuite;
import org.liquigraph.core.model.Changeset;
import org.liquigraph.core.model.Precondition;
import org.slf4j.bridge.SLF4JBridgeHandler;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.function.Supplier;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;

public abstract class ReplaceChecksumWriterTestSuite implements GraphIntegrationTestSuite {

    static {
        SLF4JBridgeHandler.removeHandlersForRootLogger();
    }

    private ConnectionSupplier connectionSupplier = new ConnectionSupplier();
    private ReplaceChecksumWriter writer;
    private Connection connection;

    @Before
    public void prepare() {
        connection = connectionSupplier.get();
        writer = new ReplaceChecksumWriter(connection);
    }

    @After
    public void close() throws SQLException {
        connection.close();
    }

    @Test
    public void persists_changesets_in_graph() throws SQLException {

        Changeset changeset = changeset("identifier", "fbiville", "CREATE (n: SomeNode {text:'yeah'})");
        persistWithoutChecksum(changeset);

        writer.write(singletonList(changeset));

        assertThatPersistedChangesetHasChecksum(changeset);
    }

    private void assertThatPersistedChangesetHasChecksum(Changeset changeset) throws SQLException {
        try (Connection connection = connectionSupplier.get();
             PreparedStatement statement = connection.prepareStatement(
                 "MATCH (changeset:__LiquigraphChangeset {id: ?, author: ?}) " +
                 "RETURN changeset.checksum"
             );
        ) {
            statement.setString(1, changeset.getId());
            statement.setString(2, changeset.getAuthor());
            statement.execute();
            try (ResultSet resultSet = statement.getResultSet()) {
                assertThat(resultSet.next()).as("No such changeset in database").isTrue();
                assertThat(resultSet.getString("changeset.checksum")).as("Invalid checksum in database").isEqualTo(changeset.getChecksum());
            }
        }
    }

    private void persistWithoutChecksum(Changeset changeset) throws SQLException {
        try (Connection connection = connectionSupplier.get();
             PreparedStatement statement = connection.prepareStatement("CREATE  (cs: __LiquigraphChangeset {id: ?, author: ?})");
        ) {
            statement.setString(1, changeset.getId());
            statement.setString(2, changeset.getAuthor());
            statement.execute();
            connection.commit();
        }
    }

    private Changeset changeset(String identifier, String author, String query) {
        Collection<String> queries = singletonList(query);
        return changeset(identifier, author, queries);
    }

    private Changeset changeset(String identifier, String author, Collection<String> queries) {
        Changeset changeset = new Changeset();
        changeset.setId(identifier);
        changeset.setAuthor(author);
        changeset.setQueries(queries);
        return changeset;
    }

    private class ConnectionSupplier implements Supplier<Connection> {
        @Override
        public Connection get() {
            return graphDatabase().newConnection();
        }
    }
}
