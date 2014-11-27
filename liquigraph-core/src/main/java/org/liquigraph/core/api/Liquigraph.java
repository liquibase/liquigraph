package org.liquigraph.core.api;

import org.liquigraph.core.configuration.Configuration;
import org.liquigraph.core.parser.ChangelogParser;
import org.liquigraph.core.validation.DeclaredChangesetValidator;
import org.liquigraph.core.validation.PersistedChangesetValidator;
import org.liquigraph.core.writer.ChangelogReader;
import org.liquigraph.core.writer.GraphJdbcConnector;
import org.liquigraph.core.writer.PreconditionExecutor;
import org.liquigraph.core.writer.PreconditionPrinter;

/**
 * Liquigraph facade in charge of migration execution.
 */
public final class Liquigraph {

    private final MigrationRunner migrationRunner;

    public Liquigraph() {
        migrationRunner = new MigrationRunner(
            new GraphJdbcConnector(),
            new ChangelogParser(),
            new ChangelogReader(),
            new ChangelogDiffMaker(),
            new PreconditionExecutor(),
            new PreconditionPrinter(),
            new DeclaredChangesetValidator(),
            new PersistedChangesetValidator()
        );
    }

    /**
     * Triggers migration execution, according to the specified {@link org.liquigraph.core.configuration.Configuration}
     * instance.
     *
     * @param configuration configuration of the changelog location and graph connection parameters
     * @see org.liquigraph.core.configuration.ConfigurationBuilder to create {@link org.liquigraph.core.configuration.Configuration instances}
     */
    public void runMigrations(Configuration configuration) {
        migrationRunner.runMigrations(configuration);
    }
}
