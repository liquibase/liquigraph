package org.liquigraph.core.api;

import org.liquigraph.core.configuration.Configuration;

class UnlockTask implements Runnable {

    private final LockManager lockManager;
    private final Configuration configuration;

    public UnlockTask(LockManager lockManager, Configuration configuration) {
        this.lockManager = lockManager;
        this.configuration = configuration;
    }

    @Override
    public void run() {
        this.lockManager.deleteLock(configuration);
    }
}
