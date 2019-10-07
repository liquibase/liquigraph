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
package org.liquigraph.core.api;

import org.liquigraph.core.configuration.ExecutionContexts;
import org.liquigraph.core.model.Changeset;
import org.liquigraph.core.model.predicates.ChangesetChecksumHasChanged;
import org.liquigraph.core.model.predicates.ChangesetRunOnChange;
import org.liquigraph.core.model.predicates.Predicates;

import java.util.Collection;
import java.util.function.Predicate;

import static java.util.stream.Collectors.toList;
import static org.liquigraph.core.model.predicates.ChangesetMatchAnyExecutionContexts.BY_ANY_EXECUTION_CONTEXT;
import static org.liquigraph.core.model.predicates.ChangesetRunAlways.RUN_ALWAYS;

class ChangelogDiffMaker {

    public Collection<Changeset> computeChangesetsToInsert(ExecutionContexts executionContexts,
                                                           Collection<Changeset> declaredChangesets,
                                                           Collection<Changeset> persistedChangesets) {

        return declaredChangesets.stream()
            .filter(BY_ANY_EXECUTION_CONTEXT(executionContexts))
            .filter(executionFilter(persistedChangesets))
            .collect(toList());
    }

    @SuppressWarnings("unchecked")
    private static Predicate<Changeset> executionFilter(Collection<Changeset> persistedChangesets) {
        return Predicates.in(persistedChangesets).negate()
            .or(ChangesetRunOnChange.RUN_ON_CHANGE
                .and(ChangesetChecksumHasChanged.CHECKSUM_HAS_CHANGED(persistedChangesets)))
            .or(RUN_ALWAYS);
    }
}
