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
package org.liquigraph.examples.dagger2.di.module;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import dagger.Module;
import dagger.Provides;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.io.fs.FileUtils;

import javax.inject.Singleton;
import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;

@Module
public class DatasourceModule {

    private static final File DB_PATH = new File("target/neo4j-hello-db");


    @Provides
    @Singleton
    static DataSource dataSource() {
        HikariConfig configuration = new HikariConfig("/datasource.properties");
        return new HikariDataSource(configuration);
    }

    @Provides
    @Singleton
    static GraphDatabaseService embeddedDatabase() {
        try {
            FileUtils.deleteRecursively(DB_PATH);
            return new GraphDatabaseFactory().newEmbeddedDatabase(DB_PATH);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
