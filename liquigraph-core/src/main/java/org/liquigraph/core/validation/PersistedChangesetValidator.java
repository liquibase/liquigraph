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

import com.google.common.base.Optional;
import com.google.common.collect.FluentIterable;
import org.liquigraph.core.model.Changeset;
import org.liquigraph.core.model.predicates.ChangesetById;
import org.liquigraph.core.model.predicates.ChangesetRunOnChange;

import java.util.Collection;

import static com.google.common.base.Predicates.not;
import static com.google.common.collect.Collections2.filter;
import static com.google.common.collect.Lists.newLinkedList;
import static java.lang.String.format;

public class PersistedChangesetValidator {

    public Collection<String> validate(Collection<Changeset> declaredChangesets, Collection<Changeset> persistedChangesets) {
        Collection<Changeset> changesets = filter(declaredChangesets, not(ChangesetRunOnChange.RUN_ON_CHANGE));
        return validateChecksums(changesets, persistedChangesets);
    }

    private Collection<String> validateChecksums(Collection<Changeset> declaredChangesets, Collection<Changeset> persistedChangesets) {
        Collection<String> errors = newLinkedList();
        for (Changeset declaredChangeset : declaredChangesets) {
            Optional<Changeset> maybePersistedChangeset = FluentIterable.from(persistedChangesets)
                .firstMatch(ChangesetById.BY_ID(declaredChangeset.getId(), declaredChangeset.getAuthor()));

            if (!maybePersistedChangeset.isPresent()) {
                continue;
            }

            Changeset persistedChangeset = maybePersistedChangeset.get();
            String declaredChecksum = declaredChangeset.getChecksum();
            String persistedChecksum = persistedChangeset.getChecksum();
            if (!persistedChecksum.equals(declaredChecksum)) {
                errors.add(
                    checksumMismatchError(declaredChangeset, persistedChangeset)
                );
            }
        }
        return errors;

    }

    private String checksumMismatchError(Changeset declaredChangeset, Changeset persistedChangeset) {
        return format(
            "Changeset with ID <%s> and author <%s> has conflicted checksums.%n\t - Declared: <%s>%n\t - Persisted: <%s>.",
            declaredChangeset.getId(), declaredChangeset.getAuthor(), declaredChangeset.getChecksum(),
            persistedChangeset.getChecksum()
        );
    }

}
