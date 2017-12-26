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

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import org.liquigraph.core.configuration.ExecutionContexts;
import org.liquigraph.core.model.Changeset;
import org.liquigraph.core.model.predicates.ChangesetChecksumHasChanged;
import org.liquigraph.core.model.predicates.ChangesetRunOnChange;

import java.util.Collection;

import static com.google.common.base.Predicates.in;
import static com.google.common.base.Predicates.not;
import static com.google.common.base.Predicates.or;
import static com.google.common.collect.FluentIterable.from;
import static org.liquigraph.core.model.predicates.ChangesetMatchAnyExecutionContexts.BY_ANY_EXECUTION_CONTEXT;
import static org.liquigraph.core.model.predicates.ChangesetRunAlways.RUN_ALWAYS;

class ChangelogDiffMaker {

    public Collection<Changeset> computeChangesetsToInsert(ExecutionContexts executionContexts,
                                                           Collection<Changeset> declaredChangesets,
                                                           Collection<Changeset> persistedChangesets) {

        return diff(executionContexts, declaredChangesets, persistedChangesets);
    }

    private static Collection<Changeset> diff(ExecutionContexts executionContexts,
                                       Collection<Changeset> declaredChangesets,
                                       Collection<Changeset> persistedChangesets) {

        return from(declaredChangesets)
            .filter(BY_ANY_EXECUTION_CONTEXT(executionContexts))
            .filter(executionFilter(persistedChangesets))
            .toList();
    }

    @SuppressWarnings("unchecked")
    private static Predicate<Changeset> executionFilter(Collection<Changeset> persistedChangesets) {
        return or(
            not(in(persistedChangesets)),
            Predicates.and(
                ChangesetRunOnChange.RUN_ON_CHANGE,
                ChangesetChecksumHasChanged.CHECKSUM_HAS_CHANGED(persistedChangesets)
            ),
            RUN_ALWAYS
        );
    }
}
