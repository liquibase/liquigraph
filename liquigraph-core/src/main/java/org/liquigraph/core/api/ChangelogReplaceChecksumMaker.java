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
package org.liquigraph.core.api;

import org.liquigraph.core.configuration.ExecutionContexts;
import org.liquigraph.core.model.Changeset;

import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;
import static org.liquigraph.core.model.predicates.ChangesetChecksumIsEmpty.CHECKSUM_IS_EMPTY;

class ChangelogReplaceChecksumMaker {

    public Collection<Changeset> computeChangesetsToUpdate(ExecutionContexts executionContexts,
                                                           Collection<Changeset> declaredChangesets,
                                                           Collection<Changeset> persistedChangesets) {

        return declaredChangesets.stream()
            .filter(emptyChecksum(persistedChangesets))
            .collect(toList());
    }

    @SuppressWarnings("unchecked")
    private static Predicate<Changeset> emptyChecksum(Collection<Changeset> persistedChangesets) {
        List<Changeset> emptyChecksum = persistedChangesets.stream().filter(CHECKSUM_IS_EMPTY).collect(Collectors.toList());
        return (c) -> emptyChecksum.contains(c);
    }
}
