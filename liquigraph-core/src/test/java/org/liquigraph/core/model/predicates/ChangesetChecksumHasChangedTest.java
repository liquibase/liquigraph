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

import java.util.Collections;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.liquigraph.core.model.predicates.ChangesetChecksumHasChanged.CHECKSUM_HAS_CHANGED;

public class ChangesetChecksumHasChangedTest {
    @Test
    public void should_match_a_persisted_changeset_with_changed_checksum() {
        Changeset changeset = changeset("identifier", "author", "MATCH (n) RETURN n");
        Predicate<Changeset> checksumHasChanged =
                CHECKSUM_HAS_CHANGED(singletonList(changeset(changeset.getId(), changeset.getAuthor(), "CREATE (n)")));

        assertThat(checksumHasChanged.apply(changeset)).isTrue();
    }

    @Test
    public void should_not_match_a_persisted_changeset_with_unchanged_checksum() {
        Changeset changeset = changeset("identifier", "author", "MATCH (n) RETURN n");
        Predicate<Changeset> checksumHasChanged = CHECKSUM_HAS_CHANGED(singletonList(changeset));

        assertThat(checksumHasChanged.apply(changeset)).isFalse();
    }

    @Test
    public void should_not_match_a_changeset_not_persisted() {
        Changeset changeset = changeset("identifier", "author", "MATCH (n) RETURN n");
        Predicate<Changeset> checksumHasChanged = CHECKSUM_HAS_CHANGED(Collections.<Changeset>emptyList());

        assertThat(checksumHasChanged.apply(changeset)).isFalse();
    }

    private Changeset changeset(String id, String author, String query) {
        Changeset changeset = new Changeset();
        changeset.setId(id);
        changeset.setAuthor(author);
        changeset.setQueries(singletonList(query));
        return changeset;
    }
}
