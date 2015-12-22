package org.liquigraph.core.api;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import org.liquigraph.core.configuration.ExecutionContexts;
import org.liquigraph.core.model.Changeset;
import org.liquigraph.core.model.predicates.ChangesetChecksumHasChanged;
import org.liquigraph.core.model.predicates.ChangesetMatchAnyExecutionContexts;
import org.liquigraph.core.model.predicates.ChangesetRunOnChange;

import java.util.Collection;

import static com.google.common.base.Predicates.or;
import static com.google.common.base.Predicates.not;
import static com.google.common.base.Predicates.in;
import static com.google.common.collect.FluentIterable.from;
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
            .filter(ChangesetMatchAnyExecutionContexts.BY_ANY_EXECUTION_CONTEXT(executionContexts))
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
