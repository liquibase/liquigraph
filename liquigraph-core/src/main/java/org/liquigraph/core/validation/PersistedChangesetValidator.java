/*
 * Copyright 2014-2022 the original author or authors.
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

import org.liquigraph.core.model.Changeset;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static org.liquigraph.core.model.predicates.ChangesetById.BY_ID;
import static org.liquigraph.core.model.predicates.ChangesetRunOnChange.RUN_ON_CHANGE;

public class PersistedChangesetValidator {

    public Collection<String> validate(Collection<Changeset> declaredChangesets, Collection<Changeset> persistedChangesets) {
        List<Changeset> changesets = declaredChangesets.stream()
            .filter(RUN_ON_CHANGE.negate())
            .collect(Collectors.toList());

        return validateChecksums(changesets, persistedChangesets);
    }

    private Collection<String> validateChecksums(Collection<Changeset> declaredChangesets, Collection<Changeset> persistedChangesets) {
        Collection<String> errors = new ArrayList<>();
        for (Changeset declaredChangeset : declaredChangesets) {
            Optional<Changeset> maybePersistedChangeset = persistedChangesets.stream()
                .filter(BY_ID(declaredChangeset.getId(), declaredChangeset.getAuthor()))
                .findFirst();

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
