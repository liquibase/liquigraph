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
package org.liquigraph.core.model.predicates;

import com.google.common.base.Predicate;
import org.junit.Test;
import org.liquigraph.core.model.Changeset;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

public class ChangesetByIdTest {
    @Test
    public void should_match_with_same_id_and_author() {
        Changeset changeset = changeset("identifier", "author", "MATCH (n) RETURN n");
        Predicate<Changeset> byId = ChangesetById.BY_ID("identifier", "author");

        assertThat(byId.apply(changeset)).isTrue();
    }

    @Test
    public void should_not_match_with_same_id_but_different_author() {
        Changeset changeset = changeset("identifier", "author", "MATCH (n) RETURN n");
        Predicate<Changeset> byId = ChangesetById.BY_ID("identifier", "author2");

        assertThat(byId.apply(changeset)).isFalse();
    }

    @Test
    public void should_not_match_with_same_author_but_different_id() {
        Changeset changeset = changeset("identifier", "author", "MATCH (n) RETURN n");
        Predicate<Changeset> byId = ChangesetById.BY_ID("identifier2", "author");

        assertThat(byId.apply(changeset)).isFalse();
    }

    @Test(expected = IllegalStateException.class)
    public void should_fail_with_no_identifier() {
        ChangesetById.BY_ID(null, "author");
    }

    @Test(expected = IllegalStateException.class)
    public void should_fail_with_no_author() {
        ChangesetById.BY_ID("identifier", null);
    }

    private Changeset changeset(String id, String author, String query) {
        Changeset changeset = new Changeset();
        changeset.setId(id);
        changeset.setAuthor(author);
        changeset.setQueries(singletonList(query));
        return changeset;
    }
}
