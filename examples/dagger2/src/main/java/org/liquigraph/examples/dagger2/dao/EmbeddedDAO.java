package org.liquigraph.examples.dagger2.dao;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Result;

import java.util.Map;

public class EmbeddedDAO implements DAO {

    private GraphDatabaseService embeddedDB;

    public EmbeddedDAO(GraphDatabaseService embeddedDB) {
        this.embeddedDB = embeddedDB;
    }

    @Override
    public String executeQuery(String query) {
        Result result = embeddedDB.execute(query);
        Map<String, Object> resultRow = result.next();
        return resultRow.get("result").toString();
    }
}
