package org.liquigraph.core.api;

import com.google.common.base.Joiner;
import org.liquigraph.core.configuration.Configuration;
import org.liquigraph.core.model.Changeset;
import org.liquigraph.core.parser.ChangelogParser;
import org.liquigraph.core.validation.PersistedChangesetValidator;
import org.liquigraph.core.validation.XmlSchemaValidator;
import org.liquigraph.core.writer.*;

import java.sql.Connection;
import java.util.Collection;

class MigrationRunner {

    private final GraphJdbcConnector connector;
    private final ChangelogParser changelogParser;
    private final ChangelogGraphReader changelogReader;
    private final ChangelogDiffMaker changelogDiffMaker;
    private final PreconditionExecutor preconditionExecutor;
    private final PreconditionPrinter preconditionPrinter;
    private final XmlSchemaValidator xmlSchemaValidator;
    private final PersistedChangesetValidator persistedChangesetValidator;

    public MigrationRunner(GraphJdbcConnector connector,
                           ChangelogParser changelogParser,
                           ChangelogGraphReader changelogGraphReader,
                           ChangelogDiffMaker changelogDiffMaker,
                           PreconditionExecutor preconditionExecutor,
                           PreconditionPrinter preconditionPrinter,
                           XmlSchemaValidator xmlSchemaValidator,
                           PersistedChangesetValidator persistedChangesetValidator) {

        this.connector = connector;
        this.changelogParser = changelogParser;
        this.changelogReader = changelogGraphReader;
        this.changelogDiffMaker = changelogDiffMaker;
        this.preconditionExecutor = preconditionExecutor;
        this.preconditionPrinter = preconditionPrinter;
        this.xmlSchemaValidator = xmlSchemaValidator;
        this.persistedChangesetValidator = persistedChangesetValidator;
    }



    public void runMigrations(Configuration configuration) {
        Collection<Changeset> declaredChangesets = parseChangesets(configuration.classLoader(), configuration.masterChangelog());

        Connection connection = connector.connect(configuration);
        Collection<Changeset> persistedChangesets = readPersistedChangesets(declaredChangesets, connection);

        Collection<Changeset> changelogsToInsert = changelogDiffMaker.computeChangesetsToInsert(
            configuration.executionContexts(),
            declaredChangesets,
            persistedChangesets
        );

        writeDiff(configuration, connection, changelogsToInsert);
    }

    private Collection<Changeset> parseChangesets(ClassLoader classLoader, String masterChangelog) {

        return changelogParser.parse(classLoader, masterChangelog);
    }

    private Collection<Changeset> readPersistedChangesets(Collection<Changeset> declaredChangesets, Connection graphDatabase) {
        Collection<Changeset> persistedChangesets = changelogReader.read(graphDatabase);
        Collection<String> errors = persistedChangesetValidator.validate(declaredChangesets, persistedChangesets);
        if (!errors.isEmpty()) {
            throw new IllegalArgumentException(formatErrorMessage(errors));
        }
        return persistedChangesets;
    }

    private void writeDiff(Configuration configuration, Connection connection, Collection<Changeset> changelogsToInsert) {
        ChangelogWriter changelogWriter = configuration.resolveWriter(
            connection,
            preconditionExecutor,
            preconditionPrinter
        );
        changelogWriter.write(changelogsToInsert);
    }

    private String formatErrorMessage(Collection<String> errors) {
        String separator = "\n\t";
        return separator + Joiner.on(separator).join(errors);
    }
}
