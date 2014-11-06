package com.liquigraph.core.writer;

import com.google.common.base.Optional;
import com.liquigraph.core.configuration.Configuration;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.rest.graphdb.RestGraphDatabase;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Paths;

import static java.lang.String.format;

public class GraphConnector {

    public final GraphDatabaseService connect(Configuration configuration) {
        String uri = configuration.uri();
        if (uri.startsWith("file:")) {
            return embeddedGraphDatabase(uri);
        }
        return serverGraphDatabase(configuration, uri);
    }

    private GraphDatabaseService embeddedGraphDatabase(String uri) {
        try {
            String path = getPath(uri);
            return new GraphDatabaseFactory().newEmbeddedDatabase(path);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(format("'uri' is invalid for embedded graph database. Given: <%s>", uri));
        }
    }

    private String getPath(String uri) throws URISyntaxException {
        String path = new URI(uri).getPath();
        if (!new File(path).exists()) {
            throw new IllegalArgumentException(format("'uri' points to an invalid location for embedded graph database. Given: <%s>", uri));
        }
        return path;
    }

    private GraphDatabaseService serverGraphDatabase(Configuration configuration, String uri) {
        Optional<String> maybeUsername = configuration.username();
        if (maybeUsername.isPresent()) {
            return new RestGraphDatabase(uri, maybeUsername.get(), configuration.password().or(""));
        }
        return new RestGraphDatabase(uri);
    }
}
