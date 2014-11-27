package org.liquigraph.core.model;

import java.util.Collection;

import static com.google.common.base.Preconditions.checkState;

public class CompoundQueries {

    public static void checkQueryListState(Collection<PreconditionQuery> queries) {
        checkState(queries.size() == 2);
    }
}
