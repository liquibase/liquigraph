package com.liquigraph.core.graph;

import com.liquigraph.core.model.PreconditionErrorPolicy;

class PreconditionResult {

    private final PreconditionErrorPolicy errorPolicy;
    private final boolean executedSuccessfully;

    public PreconditionResult(PreconditionErrorPolicy errorPolicy,
                              boolean executedSuccessfully) {

        this.errorPolicy = errorPolicy;
        this.executedSuccessfully = executedSuccessfully;
    }

    public PreconditionErrorPolicy errorPolicy() {
        return errorPolicy;
    }

    public boolean executedSuccessfully() {
        return executedSuccessfully;
    }

}
