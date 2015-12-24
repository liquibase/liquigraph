package org.liquigraph.core.api;

import com.google.common.annotations.VisibleForTesting;
import org.liquigraph.core.configuration.Configuration;
import org.liquigraph.core.io.ChangelogGraphReader;
import org.liquigraph.core.io.GraphJdbcConnector;
import org.liquigraph.core.io.LiquigraphJdbcConnector;
import org.liquigraph.core.io.PreconditionExecutor;
import org.liquigraph.core.io.PreconditionPrinter;
import org.liquigraph.core.io.xml.ChangelogParser;
import org.liquigraph.core.io.xml.ChangelogPreprocessor;
import org.liquigraph.core.io.xml.ImportResolver;
import org.liquigraph.core.io.xml.XmlSchemaValidator;
import org.liquigraph.core.validation.PersistedChangesetValidator;

/**
 * Liquigraph facade in charge of migration execution.
 */
public final class Liquigraph {

    private final MigrationRunner migrationRunner;

    public Liquigraph() {
        this(new GraphJdbcConnector());
    }

    @VisibleForTesting
    Liquigraph(LiquigraphJdbcConnector connector) {
        migrationRunner = new MigrationRunner(
                connector,
                new ChangelogParser(new XmlSchemaValidator(), new ChangelogPreprocessor(new ImportResolver())),
                new ChangelogGraphReader(),
                new ChangelogDiffMaker(),
                new PreconditionExecutor(),
                new PreconditionPrinter(),
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
