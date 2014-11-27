package org.liquigraph.core.model.predicates;

import com.google.common.base.Predicate;
import org.liquigraph.core.model.Changeset;

public enum ChangesetRunOnChange implements Predicate<Changeset> {
    RUN_ON_CHANGE;

    @Override
    public boolean apply(Changeset input) {
        return input.isRunOnChange();
    }
}
