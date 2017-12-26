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
package org.liquigraph.core.configuration;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import org.liquigraph.core.model.predicates.ExecutionContextsMatchAnyContext;

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
