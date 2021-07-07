/*
 * Copyright 2014-2021 the original author or authors.
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

import org.junit.Test;

import java.util.Collections;
import java.util.Optional;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.liquigraph.core.configuration.ExecutionContexts.DEFAULT_CONTEXT;

public class ExecutionContextsTest {

    @Test
    public void no_configured_contexts_matches_any_changeset_contexts() {
        assertThat(DEFAULT_CONTEXT.matches(Optional.empty())).isTrue();
        assertThat(DEFAULT_CONTEXT.matches(Optional.of(Collections.<String>emptyList()))).isTrue();
        assertThat(DEFAULT_CONTEXT.matches(Optional.of(singletonList("foo")))).isTrue();
    }

    @Test
    public void changesets_with_no_context_always_match() {
        assertThat(new ExecutionContexts(singletonList("foo")).matches(Optional.empty())).isTrue();
    }

    @Test
    public void changesets_with_at_least_1_matching_context_will_always_match() {
        ExecutionContexts executionContexts = new ExecutionContexts(asList("foo", "bar"));

        assertThat(executionContexts.matches(Optional.of(singletonList("foo")))).isTrue();
        assertThat(executionContexts.matches(Optional.of(singletonList("bar")))).isTrue();
        assertThat(executionContexts.matches(Optional.of(asList("foo", "bar")))).isTrue();
        assertThat(executionContexts.matches(Optional.of(asList("foo", "baz")))).isTrue();
        assertThat(executionContexts.matches(Optional.of(singletonList("baz")))).isFalse();
    }
}
