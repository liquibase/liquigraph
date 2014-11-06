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

    public Collection<String> validate(Collection<Changeset> changesets) {
        Collection<String> errors = newLinkedList();
        errors.addAll(validateMandatoryAttributes(changesets));
        errors.addAll(validateIdUniqueness(changesets));
        return errors;
    }

    private Collection<String> validateMandatoryAttributes(Collection<Changeset> changesets) {
        Collection<String> errors = newLinkedList();
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
        return errors;
    }

    private Collection<String> validateIdUniqueness(Collection<Changeset> declaredChangesets) {
        Collection<String> errors = newLinkedList();
        Collection<String> repeatedIds = repeatedIds(declaredChangesets);
        if (!repeatedIds.isEmpty()) {
            errors.add(format("<%s> is/are declared more than once.", Joiner.on(",").join(repeatedIds)));
        }
        return errors;
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
}
