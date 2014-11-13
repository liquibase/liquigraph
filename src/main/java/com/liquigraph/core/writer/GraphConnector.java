package com.liquigraph.core.writer;

import com.google.common.base.Optional;
import com.liquigraph.core.configuration.Configuration;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.rest.graphdb.RestGraphDatabase;

public class GraphConnector {

    public final GraphDatabaseService connect(Configuration configuration) {
        GraphDatabaseService graphDatabaseService = configuration.graphDatabaseService();
        if (graphDatabaseService != null) {
            return graphDatabaseService;
        }
        return serverGraphDatabase(configuration, configuration.uri());
    }

    private GraphDatabaseService serverGraphDatabase(Configuration configuration, String uri) {
        Optional<String> maybeUsername = configuration.username();
        if (maybeUsername.isPresent()) {
            return new RestGraphDatabase(uri, maybeUsername.get(), configuration.password().or(""));
        }
        return new RestGraphDatabase(uri);
    }
}
