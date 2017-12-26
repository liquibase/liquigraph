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
package org.liquigraph.core.validation;

import com.google.common.collect.Lists;
import org.junit.Test;
import org.liquigraph.core.model.Changeset;

import java.util.Collection;

import static com.google.common.collect.Lists.newArrayList;
import static java.lang.String.format;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.liquigraph.core.model.Checksums.checksum;

public class PersistedChangesetValidatorTest {

    private PersistedChangesetValidator validator = new PersistedChangesetValidator();

    @Test
    public void passes_if_nothing_persisted_yet() {
        Collection<String> errors = validator.validate(
            newArrayList(changeset("identifier", "author", "MATCH m RETURN m")),
            Lists.<Changeset>newArrayList()
        );

        assertThat(errors).isEmpty();
    }

    @Test
    public void passes_if_all_existing_changesets_have_not_changed_checksum() {
        Collection<String> errors = validator.validate(
            newArrayList(changeset("identifier", "author", "MATCH m RETURN m")),
            newArrayList(changeset("identifier", "author", "MATCH m RETURN m"))
        );

        assertThat(errors).isEmpty();
    }

    @Test
    public void passes_if_changesets_have_modified_checksums_but_run_on_change() {
        Collection<String> errors = validator.validate(
            newArrayList(changeset("identifier", "author", "MATCH m2 RETURN m2", true)),
            newArrayList(changeset("identifier", "author", "MATCH m RETURN m"))
        );

        assertThat(errors).isEmpty();
    }

    @Test
    public void fails_if_changesets_with_same_id_have_different_checksums() throws Exception {
        Collection<String> errors = validator.validate(
            newArrayList(changeset("identifier", "author", "MATCH m RETURN m")),
            newArrayList(changeset("identifier", "author", "MATCH (m)-->(z) RETURN m, z"))
        );

        assertThat(errors).containsExactly(
            format(
                "Changeset with ID <identifier> and author <author> has conflicted checksums.%n" +
                "\t - Declared: <%s>%n" +
                "\t - Persisted: <%s>.",
                checksum(singletonList("MATCH m RETURN m")),
                checksum(singletonList("MATCH (m)-->(z) RETURN m, z"))
            )
        );
    }

    private Changeset changeset(String identifier, String author, String query, boolean runOnChange) {
        Changeset changeset = changeset(identifier, author, query);
        changeset.setRunOnChange(runOnChange);
        return changeset;
    }

    private Changeset changeset(String id, String author, String query) {
        Changeset changeset = new Changeset();
        changeset.setId(id);
        changeset.setAuthor(author);
        changeset.setQueries(singletonList(query));
        return changeset;
    }
}
