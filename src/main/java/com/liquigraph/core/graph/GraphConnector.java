package com.liquigraph.core.graph;

import com.google.common.base.Optional;
import com.liquigraph.core.configuration.Configuration;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.rest.graphdb.RestGraphDatabase;

import java.net.URI;
import java.net.URISyntaxException;

import static java.lang.String.format;

public class GraphConnector {

    public GraphDatabaseService connect(Configuration configuration) {
        String uri = configuration.uri();
        if (uri.startsWith("file://")) {
            return embeddedGraphDatabase(uri);
        }
        if (uri.startsWith("http://") || uri.startsWith("https://")) {
            return serverGraphDatabase(configuration, uri);
        }
        throw new IllegalArgumentException(format("'uri' is invalid. Given: %s. Aborting now.", uri));
    }

    private GraphDatabaseService embeddedGraphDatabase(String uri) {
        try {
            return new GraphDatabaseFactory().newEmbeddedDatabase(new URI(uri).getPath());
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(format("'uri' is invalid for embedded graph database. Given: <%s>", uri));
        }
    }

    private GraphDatabaseService serverGraphDatabase(Configuration configuration, String uri) {
        Optional<String> maybeUsername = configuration.username();
        if (maybeUsername.isPresent()) {
            return new RestGraphDatabase(uri, maybeUsername.get(), configuration.password().or(""));
        }
        return new RestGraphDatabase(uri);
    }
}
