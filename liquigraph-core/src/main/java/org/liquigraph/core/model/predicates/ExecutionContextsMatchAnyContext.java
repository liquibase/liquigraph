package org.liquigraph.core.model.predicates;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;

import java.util.Collection;

public class ExecutionContextsMatchAnyContext implements Predicate<String> {
    private final Optional<Collection<String>> contexts;

    private ExecutionContextsMatchAnyContext(Optional<Collection<String>> contexts) {
        this.contexts = contexts;
    }

    public static Predicate<String> BY_ANY_CONTEXT(Optional<Collection<String>> contexts) {
        return new ExecutionContextsMatchAnyContext(contexts);
    }

    @Override
    public boolean apply(String input) {
        return !contexts.isPresent() || contexts.get().contains(input);
    }
}
