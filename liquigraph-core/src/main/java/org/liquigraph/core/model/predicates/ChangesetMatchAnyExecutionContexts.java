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
package org.liquigraph.core.model.predicates;

import com.google.common.base.Predicate;
import org.liquigraph.core.configuration.ExecutionContexts;
import org.liquigraph.core.model.Changeset;

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
