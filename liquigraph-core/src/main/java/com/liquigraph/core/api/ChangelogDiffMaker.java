package com.liquigraph.core.api;

import com.liquigraph.core.configuration.ExecutionContexts;
import com.liquigraph.core.model.Changeset;

import java.util.Collection;

import static com.google.common.base.Predicates.*;
import static com.google.common.collect.FluentIterable.from;
import static com.liquigraph.core.model.predicates.ChangesetChecksumHasChanged.CHECKSUM_HAS_CHANGED;
import static com.liquigraph.core.model.predicates.ChangesetMatchAnyExecutionContexts.BY_ANY_EXECUTION_CONTEXT;
import static com.liquigraph.core.model.predicates.ChangesetRunAlways.RUN_ALWAYS;
import static com.liquigraph.core.model.predicates.ChangesetRunOnChange.RUN_ON_CHANGE;

class ChangelogDiffMaker {

    public Collection<Changeset> computeChangesetsToInsert(ExecutionContexts executionContexts,
                                                           Collection<Changeset> declaredChangesets,
                                                           Collection<Changeset> persistedChangesets) {

        return diff(executionContexts, declaredChangesets, persistedChangesets);
    }

    private Collection<Changeset> diff(ExecutionContexts executionContexts,
                                       Collection<Changeset> declaredChangesets,
                                       Collection<Changeset> persistedChangesets) {

        return from(declaredChangesets)
            .filter(BY_ANY_EXECUTION_CONTEXT(executionContexts))
            .filter(
                or(
                    not(in(persistedChangesets)),
                    and(
                        RUN_ON_CHANGE,
                        CHECKSUM_HAS_CHANGED(persistedChangesets)
                    ),
                    RUN_ALWAYS
                )
            )
            .toList();
    }
}
