package com.liquigraph.core.validation;

import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.collect.FluentIterable;
import com.liquigraph.core.model.Changeset;
import com.liquigraph.core.model.predicates.ChangesetById;

import java.util.Collection;

import static com.google.common.base.Predicates.not;
import static com.google.common.collect.Collections2.filter;
import static com.google.common.collect.Iterables.get;
import static com.google.common.collect.Lists.newLinkedList;
import static com.liquigraph.core.model.predicates.ChangesetRunOnChange.RUN_ON_CHANGE;
import static java.lang.String.format;

public class PersistedChangesetValidator {

    public void validate(Collection<Changeset> declaredChangesets, Collection<Changeset> persistedChangesets) {
        Collection<String> errors = newLinkedList();
        validateChecksums(filter(declaredChangesets, not(RUN_ON_CHANGE)), persistedChangesets, errors);
        validateOrder(declaredChangesets, persistedChangesets, errors);

        if (!errors.isEmpty()) {
            throw new IllegalArgumentException(formatErrorMessage(errors));
        }
    }

    private void validateChecksums(Collection<Changeset> declaredChangesets, Collection<Changeset> persistedChangesets, Collection<String> errors) {
        for (Changeset declaredChangeset : declaredChangesets) {
            Optional<Changeset> maybePersistedChangeset = FluentIterable.from(persistedChangesets)
                .firstMatch(ChangesetById.BY_ID(declaredChangeset.getId()));

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

    }

    private void validateOrder(Collection<Changeset> declaredChangesets, Collection<Changeset> persistedChangesets, Collection<String> errors) {
        int difference = declaredChangesets.size() - persistedChangesets.size();
        if (difference < 0) {
            errors.add(format("At least %d declared changeset(s) is/are missing.", Math.abs(difference)));
            return;
        }
        for (int i = 0; i < persistedChangesets.size(); i++) {
            Changeset declared = get(declaredChangesets, i);
            Changeset persisted = get(persistedChangesets, i);
            String persistedId = persisted.getId();
            String declaredId = declared.getId();
            if (!declaredId.equals(persistedId)) {
                errors.add(idMismatchError(i, persistedId, declaredId));
            }
        }

    }

    private String checksumMismatchError(Changeset declaredChangeset, Changeset persistedChangeset) {
        return format(
            "Changeset with ID <%s> has conflicted checksums.%n\t - Declared: <%s>%n\t - Persisted: <%s>.",
            declaredChangeset.getId(), declaredChangeset.getChecksum(), persistedChangeset.getChecksum()
        );
    }

    private String idMismatchError(int i, String persistedId, String declaredId) {
        return format(
            "Declared changeset number %d should have%n\t\t - ID:\t <%s> %n\t\t - Found:\t<%s>.",
            i+1,
            persistedId,
            declaredId
        );
    }

    private String formatErrorMessage(Collection<String> errors) {
        String separator = "\n\t";
        return separator + Joiner.on(separator).join(errors);
    }
}
