package com.liquigraph.core.configuration;

import com.google.common.base.Optional;
import org.junit.Test;

import java.util.Collection;
import java.util.Collections;

import static com.google.common.base.Optional.fromNullable;
import static com.google.common.collect.Lists.newArrayList;
import static com.liquigraph.core.configuration.ExecutionContexts.DEFAULT_CONTEXT;
import static org.assertj.core.api.Assertions.assertThat;

public class ExecutionContextsTest {

    @Test
    public void no_configured_contexts_matches_any_changeset_contexts() {
        assertThat(DEFAULT_CONTEXT.matches(Optional.<Collection<String>>absent())).isTrue();
        assertThat(DEFAULT_CONTEXT.matches(fromNullable((Collection<String>) Collections.<String>emptyList()))).isTrue();
        assertThat(DEFAULT_CONTEXT.matches(fromNullable((Collection<String>)newArrayList("foo")))).isTrue();
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