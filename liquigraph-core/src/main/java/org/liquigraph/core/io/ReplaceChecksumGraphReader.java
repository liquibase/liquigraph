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

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;
import static org.liquigraph.core.model.predicates.ChangesetChecksumIsEmpty.CHECKSUM_IS_EMPTY;

public class ReplaceChecksumGraphReader implements ChangelogGraphReader {

    private final ChangelogGraphReader delegate;
    private Collection<Changeset> declaredChangesets;
    private Function<Connection, ReplaceChecksumWriter> writerSupplier = ReplaceChecksumWriter::new;

    public ReplaceChecksumGraphReader(ChangelogGraphReader delegate, Collection<Changeset> declaredChangesets) {
        this.delegate = delegate;
        this.declaredChangesets = declaredChangesets;
    }

    @Override
    public Collection<Changeset> read(Connection connection) {
        Collection<Changeset> persistedChangesets = delegate.read(connection);
        replaceEmptyChecksums(connection, persistedChangesets);
        return persistedChangesets;
    }

    private Collection<Changeset> replaceEmptyChecksums(Connection connection, Collection<Changeset> persistedChangesets) {
        Collection<Changeset> changesetsToUpdate = computeChangesetsToUpdate(
            declaredChangesets,
            persistedChangesets
        );
        Collection<Changeset> toReturn = persistedChangesets;
        if (! changesetsToUpdate.isEmpty()) {
            toReturn = new ArrayList<>(persistedChangesets);
            ReplaceChecksumWriter writer = writerSupplier.apply(connection);
            writer.write(changesetsToUpdate);
            // remove the persistedChangesets with empty checksum
            toReturn.removeAll(changesetsToUpdate);
            // replace them with the changesets with checksum
            toReturn.addAll(changesetsToUpdate);
        }
        return toReturn;
    }

    private Collection<Changeset> computeChangesetsToUpdate(Collection<Changeset> declaredChangesets,
                                                            Collection<Changeset> persistedChangesets) {

        return declaredChangesets.stream()
            .filter(emptyChecksum(persistedChangesets))
            .collect(toList());
    }

    private Predicate<Changeset> emptyChecksum(Collection<Changeset> persistedChangesets) {
        List<Changeset> emptyChecksum = persistedChangesets.stream()
                                        .filter(CHECKSUM_IS_EMPTY)
                                        .collect(Collectors.toList());
        return emptyChecksum::contains;
    }

    // for unit test purposes
    void setWriterSupplier(Function<Connection, ReplaceChecksumWriter> writerSupplier) {
        this.writerSupplier = writerSupplier;
    }
}
