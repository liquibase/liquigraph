/*
 * Copyright 2014-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.liquigraph.examples.dagger2;

import org.liquigraph.core.api.Liquigraph;
import org.liquigraph.examples.dagger2.dao.SentenceRepository;
import org.liquigraph.examples.dagger2.di.component.DaggerMigrationComponent;
import org.liquigraph.examples.dagger2.di.component.MigrationComponent;

import static spark.Spark.get;
import static spark.Spark.port;

public class Application {

    public static void main(String... args) throws Exception {
        MigrationComponent migrationComponent = DaggerMigrationComponent.create();
        Application application = new Application();
        application.executeSimpleMigration(migrationComponent);
    }

    public void executeSimpleMigration(MigrationComponent migrationComponent) {

        Liquigraph liquigraph = migrationComponent.liquigraph();

        liquigraph.runMigrations(migrationComponent.configuration());

        port(8080);
        get("/", (request, response) -> {
            SentenceRepository sentenceDao = migrationComponent.sentenceRepository();
            return sentenceDao.findOne().get().getContent();
        });

    }


}
