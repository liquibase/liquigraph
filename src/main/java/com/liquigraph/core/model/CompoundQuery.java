package com.liquigraph.core.model;

public interface CompoundQuery extends PreconditionQuery {

    public PreconditionQuery getFirstQuery();
    public PreconditionQuery getSecondQuery();
}
