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
package org.liquigraph.core.api;

import org.junit.Test;
import org.liquigraph.core.model.Changeset;

import java.util.Arrays;
import java.util.Collection;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.liquigraph.core.configuration.ExecutionContexts.DEFAULT_CONTEXT;

public class ChangelogReplaceChecksumMakerTest {

    private ChangelogReplaceChecksumMaker reader = new ChangelogReplaceChecksumMaker();

    @Test
    public void should_include_all_declared_changeset_with_null_checksum_in_db() {
        Collection<Changeset> changesets = reader.computeChangesetsToUpdate(
            DEFAULT_CONTEXT,
            Arrays.asList(
                changeset("ID", "fbiville", "CREATE n"),
                changeset("ID", "author2", "CREATE m"),
                changeset("ID2", "fbiville", "CREATE m")
            ),
            Arrays.asList(
                changeset("ID", "fbiville", "CREATE n"),
                changeset("ID", "author2", "CREATE m", null),
                changeset("ID2", "fbiville", "CREATE m")
            )
        );

        assertThat(changesets).containsExactly(
            changeset("ID", "author2", "CREATE m")
        );
        assertThat(changesets).extracting(Changeset::getChecksum).isNotNull();
    }

    @Test
    public void should_not_include_null_checksum_in_db_not_in_declared_changeset() {
        Collection<Changeset> changesets = reader.computeChangesetsToUpdate(
            DEFAULT_CONTEXT,
            Arrays.asList(
                changeset("ID", "fbiville", "CREATE n"),
                changeset("ID2", "fbiville", "CREATE m")
            ),
            Arrays.asList(
                changeset("ID", "author2", "CREATE m", null),
                changeset("ID2", "fbiville", "CREATE m")
            )
        );

        assertThat(changesets).isEmpty();
    }

    private Changeset changeset(String id, String author, String query) {
        Changeset changeset = new Changeset();
        changeset.setId(id);
        changeset.setAuthor(author);
        changeset.setQueries(singletonList(query));
        return changeset;
    }

    private Changeset changeset(String id, String author, String query, String checksum) {
        Changeset changeset = new Changeset();
        changeset.setId(id);
        changeset.setAuthor(author);
        changeset.setQueries(singletonList(query));
        changeset.setChecksum(checksum);
        return changeset;
    }
}
