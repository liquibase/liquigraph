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
import org.slf4j.bridge.SLF4JBridgeHandler;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.function.Supplier;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

public abstract class ClearChecksumWriterTestSuite implements GraphIntegrationTestSuite {

    static {
        SLF4JBridgeHandler.removeHandlersForRootLogger();
    }

    private ConnectionSupplier connectionSupplier = new ConnectionSupplier();
    private ClearChecksumWriter writer;
    private Connection connection;

    @Before
    public void prepare() {
        connection = connectionSupplier.get();
        writer = new ClearChecksumWriter(connection);
    }

    @After
    public void close() throws SQLException {
        connection.close();
    }

    @Test
    public void all_checksum_should_be_cleared() throws SQLException {

        Changeset changeset = changeset("identifier", "fbiville", "CREATE (n: SomeNode {text:'yeah'})");
        persist(changeset);

        writer.write(emptyList());

        assertThatChecksumIsEmpty(changeset);
    }

    @Test
    public void given_included_changeset_when_clear_then_verify_checksum_cleared_on_included_changeset_only() throws SQLException {

        Changeset changeset1 = changeset("identifier", "fbiville", "CREATE (n: SomeNode {text:'yeah'})");
        persist(changeset1);
        Changeset changeset2 = changeset("identifier2", "fbiville2", "CREATE (n: SomeNode {text:'yeah'})");
        persist(changeset2);

        writer.write(singletonList(changeset1.getId()));

        assertThatChecksumIsEmpty(changeset1);
        assertThatChecksumIsEqualTo(changeset2);
    }

    private void assertThatChecksumIsEmpty(Changeset changeset) throws SQLException {
        assertThat(getChecksumFromDatabase(changeset)).as("Checksum should be cleared").isNull();
    }

    private void assertThatChecksumIsEqualTo(Changeset changeset) throws SQLException {
        assertThat(getChecksumFromDatabase(changeset)).as("Checksum should be cleared").isEqualTo(changeset.getChecksum());
    }

    private String getChecksumFromDatabase(Changeset changeset) throws SQLException {
        try (Connection connection = connectionSupplier.get();
             PreparedStatement statement = connection.prepareStatement(
             "MATCH (changeset:__LiquigraphChangeset {id: ?, author: ?}) " +
             "RETURN changeset.checksum"
             )
        ) {
            statement.setString(1, changeset.getId());
            statement.setString(2, changeset.getAuthor());
            statement.execute();
            try (ResultSet resultSet = statement.getResultSet()) {
                assertThat(resultSet.next()).as("No such changeset in database").isTrue();
                return resultSet.getString("changeset.checksum");
            }
        }
    }

    private void persist(Changeset changeset) throws SQLException {
        try (Connection connection = connectionSupplier.get();
             PreparedStatement statement = connection.prepareStatement("CREATE (cs:__LiquigraphChangeset {id: ?, author: ?, checksum: ?})")
        ) {
            statement.setString(1, changeset.getId());
            statement.setString(2, changeset.getAuthor());
            statement.setString(3, changeset.getChecksum());
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
