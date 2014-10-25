package com.liquigraph.core.configuration;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.liquigraph.core.model.predicates.ExecutionContextsMatchAnyContext;

import java.util.Collection;

import static com.google.common.base.Optional.fromNullable;

public class ExecutionContexts {

    public static final ExecutionContexts DEFAULT_CONTEXT = new ExecutionContexts(Optional.<Collection<String>>absent());
    private Predicate<String> anyContext;

    public ExecutionContexts(Collection<String> executionContexts) {
        this(fromNullable(executionContexts));
    }

    private ExecutionContexts(Optional<Collection<String>> contexts) {
        anyContext = ExecutionContextsMatchAnyContext.BY_ANY_CONTEXT(contexts);
    }

    public boolean matches(Optional<Collection<String>> declaredContexts) {
        if (!declaredContexts.isPresent()) {
            return true;
        }
        Collection<String> changesetContexts = declaredContexts.get();
        if (changesetContexts.isEmpty()) {
            return true;
        }
        return FluentIterable.from(changesetContexts).anyMatch(anyContext);
    }
}
