package com.liquigraph.core.api;

import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.liquigraph.core.configuration.ExecutionContexts;
import com.liquigraph.core.model.Changeset;

import java.util.Collection;

import static com.google.common.base.Optional.fromNullable;

class ChangelogDiffMaker {

    public Collection<Changeset> computeChangesetsToInsert(ExecutionContexts executionContexts,
                                                           Collection<Changeset> declaredChangesets,
                                                           Collection<Changeset> persistedChangesets) {

        return diff(executionContexts, declaredChangesets, persistedChangesets);
    }

    private Collection<Changeset> diff(final ExecutionContexts executionContexts,
                                       Collection<Changeset> declaredChangesets,
                                       Collection<Changeset> persistedChangesets) {

        return FluentIterable.from(declaredChangesets)
            .skip(persistedChangesets.size())
            .filter(new Predicate<Changeset>() {
                @Override
                public boolean apply(Changeset input) {
                    return executionContexts.matches(fromNullable(input.getExecutionsContexts()));
                }
            })
            .toList();
    }
}
