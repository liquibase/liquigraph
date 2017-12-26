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
package org.liquigraph.core.model;

import com.google.common.testing.EqualsTester;
import org.junit.Test;

import java.util.Collections;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

public class ChangesetTest {
    @Test(expected = IllegalArgumentException.class)
    public void should_fail_with_null_queries() {
        new Changeset().setQueries(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void should_fail_with_empty_queries() {
        new Changeset().setQueries(Collections.<String>emptyList());
    }

    @Test(expected = IllegalArgumentException.class)
    public void should_fail_with_null_checksum() {
        new Changeset().setChecksum(null);
    }

    @Test
    public void should_split_execution_contexts_from_contexts() {
        Changeset changeset = new Changeset();
        changeset.setContexts("ctx1, ctx2");

        assertThat(changeset.getExecutionsContexts()).containsOnly("ctx1", "ctx2");
    }

    @Test
    public void should_join_contexts_from_execution_contexts() {
        Changeset changeset = new Changeset();
        changeset.setContexts("ctx1, ctx2");

        assertThat(changeset.getContexts()).isEqualTo("ctx1,ctx2");
    }

    @Test
    public void should_get_empty_contexts_as_default() {
        Changeset changeset = new Changeset();

        assertThat(changeset.getContexts()).isEmpty();
    }

    @Test
    public void should_have_equality_on_id_author_and_checksum() {
        new EqualsTester()
            .addEqualityGroup(
                changeset("identifier", "author", "CREATE (n)"),
                changeset("identifier", "author", "CREATE (n)"))
            .addEqualityGroup(changeset("identifier", "author", "MATCH (n) RETURN n"))
            .addEqualityGroup(changeset("identifier", "author2", "CREATE (n)"))
            .addEqualityGroup(changeset("identifier2", "author", "CREATE (n)"))
            .testEquals();
    }

    private Changeset changeset(String id, String author, String query) {
        Changeset changeset = new Changeset();
        changeset.setId(id);
        changeset.setAuthor(author);
        changeset.setQueries(singletonList(query));
        return changeset;
    }
}
