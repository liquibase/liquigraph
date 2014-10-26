package com.liquigraph.core.model.predicates;

import com.google.common.base.Predicate;
import com.liquigraph.core.configuration.ExecutionContexts;
import com.liquigraph.core.model.Changeset;

import static com.google.common.base.Optional.fromNullable;

public class ChangesetMatchAnyExecutionContexts implements Predicate<Changeset> {

    private final ExecutionContexts executionContexts;

    private ChangesetMatchAnyExecutionContexts(ExecutionContexts executionContexts) {
        this.executionContexts = executionContexts;
    }

    public static ChangesetMatchAnyExecutionContexts BY_ANY_EXECUTION_CONTEXT(ExecutionContexts executionContexts) {
        return new ChangesetMatchAnyExecutionContexts(executionContexts);
    }

    @Override
    public boolean apply(Changeset input) {
        return executionContexts.matches(fromNullable(input.getExecutionsContexts()));
    }
}
