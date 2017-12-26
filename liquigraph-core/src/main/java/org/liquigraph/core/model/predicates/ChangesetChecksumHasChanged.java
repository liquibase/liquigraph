/*
 * Copyright 2014-2018 the original author or authors.
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
package org.liquigraph.core.model.predicates;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import org.liquigraph.core.model.Changeset;

import java.util.Collection;

import static com.google.common.collect.FluentIterable.from;
import static org.liquigraph.core.model.predicates.ChangesetById.BY_ID;

public class ChangesetChecksumHasChanged implements Predicate<Changeset> {

    private final Collection<Changeset> persistedChangesets;

    private ChangesetChecksumHasChanged(Collection<Changeset> persistedChangesets) {
        this.persistedChangesets = persistedChangesets;
    }

    public static Predicate<Changeset> CHECKSUM_HAS_CHANGED(Collection<Changeset> persistedChangesets) {
        return new ChangesetChecksumHasChanged(persistedChangesets);
    }

    @Override
    public boolean apply(Changeset input) {
        Optional<Changeset> persistedChangeset =
                from(persistedChangesets).firstMatch(BY_ID(input.getId(), input.getAuthor()));
        return persistedChangeset.isPresent() && !input.getChecksum().equals(persistedChangeset.get().getChecksum());
    }
}
