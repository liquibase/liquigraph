/*
 * Copyright 2014-2022 the original author or authors.
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

import org.liquigraph.core.exception.LiquigraphLockException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * Shared lock proxy using references on the connections to create and remove the lock node, more or less like
 * performing garbage collection by reference counting. It keeps references instead of simply counting to be able to
 * remove the lock using the shutdown hook.
 */
public class LiquigraphLock {
    private static final Logger LOGGER = LoggerFactory.getLogger(LiquigraphLock.class);

    private final UUID uuid = UUID.randomUUID();
    private final Map<Connection, Boolean> connections = new IdentityHashMap<>();
    private final Thread task = new Thread(new ShutdownTask(this));

    private final Supplier<Connection> connectionSupplier;

    public LiquigraphLock(Supplier<Connection> connection) {
        this.connectionSupplier = connection;
    }

    void acquire(Connection connection) {
        if (addConnection(connection)) {
            LOGGER.debug("Acquiring lock {} on database", uuid);
            addShutdownHook();
            ensureLockUniqueness(connection);
            tryWriteLock(connection);
        }
    }

    void release(Connection connection) {
        if (removeConnection(connection)) {
            LOGGER.debug("Releasing lock {} from database", uuid);
            removeShutdownHook();
            releaseLock(connection);
        }
    }

    void cleanup() {
        for (Connection connection : new HashSet<>(this.connections.keySet())) {
            release(connection);
        }
    }

    private boolean addConnection(Connection connection) {
        boolean wasEmpty = connections.isEmpty();
        connections.put(connection, Boolean.TRUE);
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

    private void ensureLockUniqueness(Connection connection) {
        try (Statement statement = connection.createStatement()) {
            statement.execute("CREATE CONSTRAINT ON (lock:__LiquigraphLock) ASSERT lock.name IS UNIQUE");
            connection.commit();
        } catch (SQLException e) {
            throw new LiquigraphLockException(
                "Could not ensure __LiquigraphLock uniqueness\t" +
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
        } catch (SQLException e) {
            throw new LiquigraphLockException(
                "Cannot create __LiquigraphLock lock\n\t" +
                    "Likely another Liquigraph execution is going on or has crashed.",
                e
            );
        }
    }

    private void releaseLock(Connection connection) {
        String deleteLockQuery = "MATCH (lock:__LiquigraphLock {uuid:{1}}) DELETE lock";
        try (PreparedStatement statement = connection.prepareStatement(deleteLockQuery)) {
            statement.setString(1, uuid.toString());
            statement.execute();
            connection.commit();
        } catch (SQLException firstAttemptException) {
            LOGGER.info("Failed to remove __LiquigraphLock. Trying again with new connection", firstAttemptException);
            // the connection used above probably points to a failed transaction and new statements cannot be committed
            // a new connection is open so that the cleanup can actually happem
            try (PreparedStatement statement = this.connectionSupplier.get().prepareStatement(deleteLockQuery)) {
                statement.setString(1, uuid.toString());
                statement.execute();
                connection.commit();
            }
            catch (SQLException secondAttemptException) {
                LOGGER.error("Cannot remove __LiquigraphLock during cleanup.", secondAttemptException);
            }
        }
    }
}
