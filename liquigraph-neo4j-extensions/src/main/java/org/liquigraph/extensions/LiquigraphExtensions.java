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
package org.liquigraph.extensions;

import org.liquigraph.core.io.ChangelogGraphReader;
import org.liquigraph.core.io.lock.LiquigraphLock;
import org.liquigraph.core.io.lock.LockableConnection;
import org.liquigraph.core.model.Changeset;
import org.liquigraph.extensions.jdbc.GraphDatabaseServiceConnection;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.procedure.Context;
import org.neo4j.procedure.Procedure;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class LiquigraphExtensions {

    @Context
    public GraphDatabaseService graphDatabaseService;

    @Procedure(value = "liquigraph.changelog")
    public Stream<ChangesetRecord> changelog() {
        Supplier<Connection> connectionSupplier = getConnectionSupplier();
        try (Connection delegate = connectionSupplier.get();
             Connection connection = LockableConnection.acquire(delegate, new LiquigraphLock(connectionSupplier))) {

            Collection<Changeset> changesets = new ChangelogGraphReader().read(connection);
            return changesets.stream().map(ChangesetRecord::new);

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private Supplier<Connection> getConnectionSupplier() {
        return () -> new GraphDatabaseServiceConnection(graphDatabaseService);
    }
}
