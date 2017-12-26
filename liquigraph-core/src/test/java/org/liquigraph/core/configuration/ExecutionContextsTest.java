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
import org.assertj.core.api.Assertions;
import org.junit.Test;

import java.util.Collection;
import java.util.Collections;

import static com.google.common.base.Optional.fromNullable;
import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;

public class ExecutionContextsTest {

    @Test
    public void no_configured_contexts_matches_any_changeset_contexts() {
        Assertions.assertThat(ExecutionContexts.DEFAULT_CONTEXT.matches(Optional.<Collection<String>>absent())).isTrue();
        Assertions.assertThat(ExecutionContexts.DEFAULT_CONTEXT.matches(fromNullable((Collection<String>) Collections.<String>emptyList()))).isTrue();
        Assertions.assertThat(ExecutionContexts.DEFAULT_CONTEXT.matches(fromNullable((Collection<String>) newArrayList("foo")))).isTrue();
    }

    @Test
    public void changesets_with_no_context_always_match() {
        assertThat(new ExecutionContexts(newArrayList("foo")).matches(Optional.<Collection<String>>absent())).isTrue();
    }

    @Test
    public void changesets_with_at_least_1_matching_context_will_always_match() {
        ExecutionContexts executionContexts = new ExecutionContexts(newArrayList("foo", "bar"));

        assertThat(executionContexts.matches(Optional.<Collection<String>>fromNullable(newArrayList("foo")))).isTrue();
        assertThat(executionContexts.matches(Optional.<Collection<String>>fromNullable(newArrayList("bar")))).isTrue();
        assertThat(executionContexts.matches(Optional.<Collection<String>>fromNullable(newArrayList("foo", "bar")))).isTrue();
        assertThat(executionContexts.matches(Optional.<Collection<String>>fromNullable(newArrayList("foo", "baz")))).isTrue();
        assertThat(executionContexts.matches(Optional.<Collection<String>>fromNullable(newArrayList("baz")))).isFalse();
    }
}