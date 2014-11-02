package com.liquigraph.core.model;

public interface CompoundQuery extends PreconditionQuery {

    PreconditionQuery getFirstQuery();
    PreconditionQuery getSecondQuery();
    boolean compose(boolean firstResult, boolean secondResult);
}
