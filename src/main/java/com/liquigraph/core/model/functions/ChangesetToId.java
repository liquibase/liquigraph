package com.liquigraph.core.model.functions;

import com.google.common.base.Function;
import com.liquigraph.core.model.Changeset;

public enum ChangesetToId implements Function<Changeset, String> {
    INTO_ID;

    @Override
    public String apply(Changeset input) {
        return input.getId();
    }
}
