package com.liquigraph.core.api;

import com.google.common.base.Joiner;
import com.liquigraph.core.configuration.Configuration;
import com.liquigraph.core.model.Changeset;
import com.liquigraph.core.parser.ChangelogParser;
import com.liquigraph.core.validation.DeclaredChangesetValidator;
import com.liquigraph.core.validation.PersistedChangesetValidator;
import com.liquigraph.core.writer.*;

import java.sql.Connection;
import java.util.Collection;

class MigrationRunner {

    private final GraphJdbcConnector connector;
    private final ChangelogParser changelogParser;
    private final ChangelogReader changelogReader;
    private final ChangelogDiffMaker changelogDiffMaker;
    private final PreconditionExecutor preconditionExecutor;
    private final PreconditionPrinter preconditionPrinter;
    private final DeclaredChangesetValidator declaredChangesetValidator;
    private final PersistedChangesetValidator persistedChangesetValidator;

    public MigrationRunner(GraphJdbcConnector connector,
                           ChangelogParser changelogParser,
                           ChangelogReader changelogReader,
                           ChangelogDiffMaker changelogDiffMaker,
                           PreconditionExecutor preconditionExecutor,
                           PreconditionPrinter preconditionPrinter,
                           DeclaredChangesetValidator declaredChangesetValidator,
                           PersistedChangesetValidator persistedChangesetValidator) {

        this.connector = connector;
        this.changelogParser = changelogParser;
        this.changelogReader = changelogReader;
        this.changelogDiffMaker = changelogDiffMaker;
        this.preconditionExecutor = preconditionExecutor;
        this.preconditionPrinter = preconditionPrinter;
        this.declaredChangesetValidator = declaredChangesetValidator;
        this.persistedChangesetValidator = persistedChangesetValidator;
    }

    public void runMigrations(Configuration configuration) {
        Collection<Changeset> declaredChangesets = parseChangesets(configuration.masterChangelog());

        Connection connection = connector.connect(configuration);
        Collection<Changeset> persistedChangesets = readPersistedChangesets(declaredChangesets, connection);

        Collection<Changeset> changelogsToInsert = changelogDiffMaker.computeChangesetsToInsert(
            configuration.executionContexts(),
            declaredChangesets,
            persistedChangesets
        );
        writeDiff(configuration, connection, changelogsToInsert);
    }

    private Collection<Changeset> parseChangesets(String masterChangelog) {
        Collection<Changeset> declaredChangesets = changelogParser.parse(masterChangelog);
        Collection<String> errors = declaredChangesetValidator.validate(declaredChangesets);
        if (!errors.isEmpty()) {
            throw new IllegalArgumentException(formatErrorMessage(errors));
        }
        return declaredChangesets;
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
