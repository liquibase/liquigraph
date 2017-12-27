/*
 * Copyright 2014-2018 the original author or authors.
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

import org.liquigraph.examples.dagger2.configuration.DaggerDataComponent;
import org.liquigraph.examples.dagger2.configuration.DataComponent;
import org.liquigraph.examples.dagger2.configuration.DataModule;
import org.liquigraph.examples.dagger2.domain.Sentence;
import org.liquigraph.examples.dagger2.repository.LiquigraphClient;
import org.liquigraph.examples.dagger2.repository.SentenceRepository;

import static spark.Spark.get;
import static spark.Spark.port;

public class Application {

    public static void main(String... args) {
        DataComponent dataComponent = DaggerDataComponent.builder()
                .dataModule(dataModule(args))
                .build();

        LiquigraphClient liquigraphClient = dataComponent.liquigraphClient();
        liquigraphClient.run();

        Application application = new Application();
        application.serve(dataComponent.sentenceRepository());
    }

    private static DataModule dataModule(String[] args) {
        if (args.length == 0) {
            return new DataModule();
        }
        String jdbcUri = args[0];
        return new DataModule(jdbcUri);
    }

    private void serve(SentenceRepository sentenceRepository) {
        port(8080);
        get("/", (request, response) ->
                sentenceRepository.findOne()
                        .map(Sentence::getContent)
                        .orElseGet(() -> {
                            response.status(204);
                            return "";
                        }));
    }


}
