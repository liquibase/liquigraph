package org.liquigraph.examples.dagger2.di.module;

import dagger.Module;
import dagger.Provides;
import org.liquigraph.examples.dagger2.dao.DAO;
import org.liquigraph.examples.dagger2.dao.EmbeddedDAO;
import org.neo4j.graphdb.GraphDatabaseService;

import javax.inject.Singleton;

@Module(includes = {DatasourceModule.class})
public class EmbeddedDAOModule {
    @Provides
    @Singleton
    public DAO embeddedDAO(GraphDatabaseService embeddedDatabase) {
        return new EmbeddedDAO(embeddedDatabase);
    }

}
