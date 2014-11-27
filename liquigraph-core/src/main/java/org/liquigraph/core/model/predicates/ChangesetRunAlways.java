package org.liquigraph.core.model.predicates;

import com.google.common.base.Predicate;
import org.liquigraph.core.model.Changeset;

public enum ChangesetRunAlways implements Predicate<Changeset> {
    RUN_ALWAYS;

    @Override
    public boolean apply(Changeset input) {
        return input.isRunAlways();
    }
}
