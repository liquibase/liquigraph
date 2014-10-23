package com.liquigraph.core.model.predicates;

import com.google.common.base.Predicate;
import com.liquigraph.core.model.Changeset;

import static com.google.common.base.Preconditions.checkState;

public class ChangesetById implements Predicate<Changeset> {

    private final String id;

    private ChangesetById(String id) {
        this.id = id;
        checkState(id != null);
    }

    public static Predicate<Changeset> BY_ID(String id) {
        return new ChangesetById(id);
    }

    @Override
    public boolean apply(Changeset input) {
        return id.equals(input.getId());
    }
}
