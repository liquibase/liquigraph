package com.liquigraph.core.api;

import com.liquigraph.core.configuration.Configuration;
import com.liquigraph.core.graph.ChangelogReader;
import com.liquigraph.core.graph.ChangelogWriter;
import com.liquigraph.core.graph.GraphConnector;
import com.liquigraph.core.model.Changeset;
import com.liquigraph.core.parser.ChangelogParser;
import com.liquigraph.core.validation.DeclaredChangesetValidator;
import com.liquigraph.core.validation.PersistedChangesetValidator;
import org.neo4j.graphdb.GraphDatabaseService;

import java.util.Collection;

class MigrationRunner {

    private final GraphConnector connector;
    private final ChangelogParser changelogParser;
    private final ChangelogReader changelogReader;
    private final ChangelogWriter changelogWriter;
    private final ChangelogDiffMaker changelogDiffMaker;
    private final DeclaredChangesetValidator declaredChangesetValidator;
    private final PersistedChangesetValidator persistedChangesetValidator;

    public MigrationRunner(GraphConnector connector,
                           ChangelogParser changelogParser,
                           ChangelogReader changelogReader,
                           ChangelogWriter changelogWriter,
                           ChangelogDiffMaker changelogDiffMaker,
                           DeclaredChangesetValidator declaredChangesetValidator,
                           PersistedChangesetValidator persistedChangesetValidator) {

        this.connector = connector;
        this.changelogParser = changelogParser;
        this.changelogReader = changelogReader;
        this.changelogWriter = changelogWriter;
        this.changelogDiffMaker = changelogDiffMaker;
        this.declaredChangesetValidator = declaredChangesetValidator;
        this.persistedChangesetValidator = persistedChangesetValidator;
    }

    public void runMigrations(Configuration configuration) {
        Collection<Changeset> declaredChangesets = changelogParser.parse(configuration.masterChangelog());
        declaredChangesetValidator.validate(declaredChangesets);

        GraphDatabaseService graphDatabase = connector.connect(configuration);
        Collection<Changeset> persistedChangesets = changelogReader.read(graphDatabase);
        persistedChangesetValidator.validate(declaredChangesets, persistedChangesets);

        Collection<Changeset> changelogsToInsert = changelogDiffMaker.computeChangesetsToInsert(
            configuration.executionContexts(),
            declaredChangesets,
            persistedChangesets
        );

        changelogWriter.write(graphDatabase, changelogsToInsert);
    }
}
