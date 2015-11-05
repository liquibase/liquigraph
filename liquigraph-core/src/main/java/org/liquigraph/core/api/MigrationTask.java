package org.liquigraph.core.api;

import org.liquigraph.core.configuration.Configuration;

class MigrationTask implements Runnable {

    private final MigrationRunner runner;
    private final Configuration configuration;

    public MigrationTask(MigrationRunner runner,
                         Configuration configuration) {

        this.runner = runner;
        this.configuration = configuration;
    }

    @Override
    public void run() {
        runner.runMigrations(configuration);
    }
}
