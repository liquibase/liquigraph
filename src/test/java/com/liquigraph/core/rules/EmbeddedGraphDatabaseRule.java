package com.liquigraph.core.rules;

import org.junit.rules.ExternalResource;
import org.neo4j.cypher.javacompat.ExecutionEngine;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static com.google.common.base.Throwables.propagate;
import static java.nio.file.Files.walkFileTree;

public class EmbeddedGraphDatabaseRule extends ExternalResource {

    private GraphDatabaseService graphDatabase;
    private ExecutionEngine cypherEngine;
    private File graphDirectory;

    public GraphDatabaseService graphDatabase() {
        return graphDatabase;
    }

    public ExecutionEngine cypherEngine() {
        return cypherEngine;
    }

    public File graphDirectory() {
        return graphDirectory;
    }

    protected void before() throws IOException {
        graphDirectory = Files.createTempDirectory("neo").toFile();
        graphDatabase = new GraphDatabaseFactory().newEmbeddedDatabase(graphDirectory.getPath());
        cypherEngine = new ExecutionEngine(graphDatabase);
    }

    protected void after() {
        if (graphDatabase != null) {
            graphDatabase.shutdown();
        }
        if (graphDirectory != null) {
            try {
                walkFileTree(graphDirectory.toPath(), new DeletionFileVisitor());
            } catch (IOException e) {
                throw propagate(e);
            }
        }
    }
}
