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

import org.junit.Before;
import org.junit.Test;
import org.liquigraph.core.model.Changeset;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ReplaceChecksumGraphReaderTest {

    private ReplaceChecksumGraphReader reader;
    private ChangelogGraphReader delegateMock;
    private List<Changeset> declaredChangesets;
    private Connection connectionMock;
    private ReplaceChecksumWriter writerMock;

    @Before
    public void setUp() {
        writerMock = mock(ReplaceChecksumWriter.class);
        delegateMock = mock(ChangelogGraphReader.class);
        connectionMock = mock(Connection.class);
        declaredChangesets = new ArrayList<>();
        reader = new ReplaceChecksumGraphReader(delegateMock, declaredChangesets);
        reader.setWriterSupplier((c) -> writerMock);
    }

    @Test
    public void verify_delegate_reader_changesets_are_returned() {
        List<Changeset> persistedChangesets = singletonList(changeset("identifier", "fbiville", "CREATE (n: SomeNode {text:'yeah'})"));
        when(delegateMock.read(connectionMock)).thenReturn(persistedChangesets);

        Collection<Changeset> result = reader.read(connectionMock);

        assertThat(result).isSameAs(persistedChangesets);
    }

    @Test
    public void all_persisted_changesets_have_checksum_then_verify_db_not_updated() {

        Changeset changeset = changeset("identifier", "fbiville", "CREATE (n: SomeNode {text:'yeah'})");
        List<Changeset> persistedChangesets = singletonList(changeset);
        when(delegateMock.read(connectionMock)).thenReturn(persistedChangesets);

        reader.read(connectionMock);

        verify(writerMock, never()).write(any());
    }

    @Test
    public void persisted_changesets_have_empty_checksum_then_verify_db_updated() {

        declaredChangesets.add(changeset("identifier", "fbiville", "CREATE (n: SomeNode {text:'yeah'})"));
        declaredChangesets.add(changeset("identifier2", "fbiville2", "CREATE (n: SomeNode {text:'yeah2'})"));
        Changeset missingChecksumChangeset = changeset("identifier", "fbiville", "CREATE (n: SomeNode {text:'yeah'})", null);
        List<Changeset> persistedChangesets = asList(
            missingChecksumChangeset,
            changeset("identifier2", "fbiville2", "CREATE (n: SomeNode {text:'yeah2'})")
        );
        when(delegateMock.read(connectionMock)).thenReturn(persistedChangesets);

        Collection<Changeset> result = reader.read(connectionMock);

        verify(writerMock).write(singletonList(missingChecksumChangeset));
        assertThat(result).isEqualTo(persistedChangesets);
        assertThat(result).filteredOn(it -> it.equals(missingChecksumChangeset))
            .extracting("checksum")
            .isNotNull();
    }

    @Test
    public void persisted_changesets_have_empty_checksum_but_do_not_match_declared_changesets_then_verify_db_not_updated() {

        List<Changeset> persistedChangesets = singletonList(
            changeset("identifier", "fbiville", "CREATE (n: SomeNode {text:'yeah'})", null)
        );
        when(delegateMock.read(connectionMock)).thenReturn(persistedChangesets);

        Collection<Changeset> result = reader.read(connectionMock);

        verify(writerMock, never()).write(any());
        assertThat(result).isSameAs(persistedChangesets);
    }

    private Changeset changeset(String identifier, String author, String query) {
        Collection<String> queries = singletonList(query);
        return changeset(identifier, author, queries);
    }

    private Changeset changeset(String identifier, String author, String query, String checksum) {
        Collection<String> queries = singletonList(query);
        Changeset changeset = changeset(identifier, author, queries);
        changeset.setChecksum(checksum);
        return changeset;
    }

    private Changeset changeset(String identifier, String author, Collection<String> queries) {
        Changeset changeset = new Changeset();
        changeset.setId(identifier);
        changeset.setAuthor(author);
        changeset.setQueries(queries);
        return changeset;
    }
}
