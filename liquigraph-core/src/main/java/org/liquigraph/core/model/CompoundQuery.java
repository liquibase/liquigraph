package org.liquigraph.core.model;

public interface CompoundQuery extends PreconditionQuery {

    PreconditionQuery getFirstQuery();
    PreconditionQuery getSecondQuery();
    boolean compose(boolean firstResult, boolean secondResult);
    String compose(String firstQuery, String secondQuery);
}
