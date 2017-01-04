package org.liquigraph.examples.dagger2.di.component;

import dagger.Component;
import org.liquigraph.examples.dagger2.di.module.EmbeddedDAOModule;
import org.liquigraph.examples.dagger2.di.module.EmbeddedLiquigraphModule;

import javax.inject.Singleton;

/**
 * Liquigraph and DAO using an embedded neo4j database
 */
@Singleton
@Component(modules = {EmbeddedDAOModule.class, EmbeddedLiquigraphModule.class})
public interface MigrationTestComponent extends MigrationComponent {


}
