package com.liquigraph.core.validation;

import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Multiset;
import com.liquigraph.core.model.Changeset;

import java.util.Collection;

import static com.google.common.base.Predicates.notNull;
import static com.google.common.base.Strings.emptyToNull;
import static com.google.common.collect.LinkedHashMultiset.create;
import static com.google.common.collect.Lists.newLinkedList;
import static com.google.common.collect.Multisets.filter;
import static com.liquigraph.core.model.functions.ChangesetToId.INTO_ID;
import static java.lang.String.format;

public class DeclaredChangesetValidator {

    public void validate(Collection<Changeset> changesets) {
        Collection<String> errors = newLinkedList();
        validateMandatoryAttributes(changesets, errors);
        validateIdUniqueness(changesets, errors);
        if (!errors.isEmpty()) {
            throw new IllegalArgumentException(formatErrorMessage(errors));
        }
    }

    private void validateMandatoryAttributes(Collection<Changeset> changesets, Collection<String> errors) {
        int i = 1;
        for (Changeset changeset : changesets) {
            if (emptyToNull(changeset.getId()) == null) {
                errors.add(format("Changeset %d - 'id' should not be missing/blank.", i));
            }
            if (emptyToNull(changeset.getAuthor()) == null) {
                errors.add(format("Changeset %d - 'author' should not be missing/blank.", i));
            }
            if (emptyToNull(changeset.getQuery()) == null) {
                errors.add(format("Changeset %d - 'query' should not be missing/blank.", i));
            }
            i++;
        }

    }

    private void validateIdUniqueness(Collection<Changeset> declaredChangesets, Collection<String> errors) {
        Collection<String> repeatedIds = repeatedIds(declaredChangesets);
        if (!repeatedIds.isEmpty()) {
            errors.add(format("<%s> is/are declared more than once.", Joiner.on(",").join(repeatedIds)));
        }
    }

    private Collection<String> repeatedIds(Collection<Changeset> declaredChangesets) {
        final Multiset<String> ids = create(changesetIds(declaredChangesets));
        return filter(ids, new Predicate<String>() {
            @Override
            public boolean apply(String input) {
                return ids.count(input) > 1;
            }
        }).elementSet();
    }

    private Collection<String> changesetIds(Collection<Changeset> declaredChangesets) {
        return FluentIterable.from(declaredChangesets)
            .transform(INTO_ID)
            .filter(notNull())
            .toList();
    }

    private String formatErrorMessage(Collection<String> errors) {
        String separator = "\n\t";
        return separator + Joiner.on(separator).join(errors);
    }
}
