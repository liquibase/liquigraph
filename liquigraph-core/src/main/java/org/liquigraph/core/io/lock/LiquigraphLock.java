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
package org.liquigraph.core.io.lock;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;
import org.liquigraph.core.exception.LiquigraphLockException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.google.common.collect.Sets.newIdentityHashSet;

/**
 * Shared lock proxy using references on the connections to create and remove the lock node, more or less like
 * performing garbage collection by reference counting. It keeps references instead of simply counting to be able to
 * remove the lock using the shutdown hook.
 */
public class LiquigraphLock {
    private static final Logger LOGGER = LoggerFactory.getLogger(LiquigraphLock.class);

    private final UUID uuid = UUID.randomUUID();
    private final Set<Connection> connections = newIdentityHashSet();
    private final Thread task = new Thread(new ShutdownTask(this));

    void acquire(Connection connection) {
        if (addConnection(connection)) {
            addShutdownHook();
            ensureLockUnicity(connection);
            tryWriteLock(connection);
        }
    }

    void release(Connection connection) {
        if (removeConnection(connection)) {
            removeShutdownHook();
            releaseLock(connection);
        }
    }

    void cleanup() {
        for (Connection connection : new ArrayList<>(connections)) {
            release(connection);
        }
    }

    private boolean addConnection(Connection connection) {
        boolean wasEmpty = connections.isEmpty();
        connections.add(connection);
        return wasEmpty;
    }

    private boolean removeConnection(Connection connection) {
        if (connections.isEmpty()) {
            return false;
        }
        connections.remove(connection);
        return connections.isEmpty();
    }

    private void addShutdownHook() {
        Runtime.getRuntime().addShutdownHook(task);
    }

    private void removeShutdownHook() {
        Runtime.getRuntime().removeShutdownHook(task);
    }

    private void ensureLockUnicity(Connection connection) {
        try (Statement statement = connection.createStatement()) {
            statement.execute("CREATE CONSTRAINT ON (lock:__LiquigraphLock) ASSERT lock.name IS UNIQUE");
            connection.commit();
        }
        catch (SQLException e) {
            throw new LiquigraphLockException(
                    "Could not ensure __LiquigraphLock unicity\n\t" +
                            "Please make sure your instance is in a clean state\n\t" +
                            "No more than 1 lock should be there simultaneously!",
                    e
            );
        }
    }

    private void tryWriteLock(Connection connection) {
        try (PreparedStatement statement = connection.prepareStatement(
                "CREATE (:__LiquigraphLock {name:'John', uuid:{1}})")) {

            statement.setString(1, uuid.toString());
            statement.execute();
            connection.commit();
        }
        catch (SQLException e) {
            throw new LiquigraphLockException(
                    "Cannot create __LiquigraphLock lock\n\t" +
                            "Likely another Liquigraph execution is going on or has crashed.",
                    e
            );
        }
    }

    private void releaseLock(Connection connection) {
        try (PreparedStatement statement = connection.prepareStatement(
                "MATCH (lock:__LiquigraphLock {uuid:{1}}) DELETE lock")) {

            statement.setString(1, uuid.toString());
            statement.execute();
            connection.commit();
        } catch (SQLException e) {
            LOGGER.error(
                    "Cannot remove __LiquigraphLock during cleanup.",
                    e
            );
        }
    }
}
