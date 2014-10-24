package com.liquigraph.core.api;

import com.liquigraph.core.configuration.Configuration;

public final class Liquigraph {

    private final MigrationRunner migrationRunner;

    public Liquigraph(MigrationRunner migrationRunner) {
        this.migrationRunner = migrationRunner;
    }

    public void runMigrations(Configuration configuration) {
        migrationRunner.runMigrations(configuration);
    }


}
