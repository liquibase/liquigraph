package org.liquigraph.core.api;

import org.liquigraph.core.configuration.Configuration;
import org.liquigraph.core.exception.LockManagerException;
import org.liquigraph.core.writer.GraphJdbcConnector;
import org.neo4j.graphdb.Node;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static com.google.common.base.Throwables.propagate;
import static java.util.concurrent.TimeUnit.MINUTES;

/**
 * Class that manages the lock/unlock migration mechanism.
 * This ensures only one execution at a time is performed against the
 * same database instance.
 *
 * @author Benoît Simard
 * @author Florent Biville
 */
class LockManager {

    private static final String CREATE_QUERY =   "CREATE (lock:__LiquigraphLock) SET lock.ip={0}, lock.date={1} RETURN lock";
    private static final String FIND_QUERY =     "MATCH (lock:__LiquigraphLock) RETURN lock";
    private static final String DELETE_QUERY =   "MATCH (lock:__LiquigraphLock) DELETE lock";

    private final Lock lock;
    private final Condition absentLock;
    private final Condition presentLock;

    private final GraphJdbcConnector connector;

    public LockManager(GraphJdbcConnector connector) {
        this.connector = connector;

        this.lock = new ReentrantLock();
        this.absentLock = lock.newCondition();
        this.presentLock = lock.newCondition();
    }

    public void lockAndExecute(Runnable task, Configuration configuration) throws InterruptedException {
        lock.tryLock(1, MINUTES);

        try {
            while (isLockInDatabase(configuration)) {
                absentLock.await();
            }

            insertLock(configuration);
            registerDeleteLockShutDownHook(new UnlockTask(this, configuration));

            task.run();

            presentLock.signal();
        }
        catch (SQLException e) {
            throw new LockManagerException("A Cypher exception occurred when locking", e);
        }
        finally {
            deleteLock(configuration);
            absentLock.signal();
            lock.unlock();
        }
    }

    void deleteLock(Configuration configuration) {
        try {
            executeQuery(configuration, DELETE_QUERY);
        }
        catch (SQLException e) {
            throw new LockManagerException("A Cypher exception occurred when unlocking", e);
        }
    }

    private static String ipOrDefault(String defaultValue) {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            return defaultValue;
        }
    }

    private static String currentFormattedDate(String format) {
        DateFormat dateFormat = new SimpleDateFormat(format);
        Calendar cal = Calendar.getInstance();
        return dateFormat.format(cal.getTime());
    }

    private boolean isLockInDatabase(Configuration configuration) {
        try {
            return !executeQuery(configuration, FIND_QUERY).isEmpty();
        } catch (SQLException e) {
            throw new LockManagerException("A Cypher exception occurred when checking lock", e);
        }
    }

    private void insertLock(Configuration configuration) throws SQLException {
        executeQuery(configuration, CREATE_QUERY, Arrays.asList(ipOrDefault("127.0.0.1"),currentFormattedDate("yyyy/MM/dd HH:mm:ss")));
    }

    private Map<String, Object> executeQuery(Configuration configuration, String query) throws SQLException {
        return executeQuery(configuration, query, Collections.<String>emptyList());
    }

    private Map<String, Object> executeQuery(Configuration configuration, String query, List<String> args) throws SQLException {
        try (Connection connection = connector.connect(configuration);
            PreparedStatement statement = connection.prepareStatement(query)) {
            setArguments(args, statement);
            if (statement.execute()) {
                return asMap(statement);
            }
            return new HashMap<>();
        }
        catch (SQLException e) {
            throw propagate(e);
        }
    }

    private static void setArguments(List<String> args, PreparedStatement statement) throws SQLException {
        int i = 0;
        for (String arg : args) {
            statement.setObject(i++, arg);
        }
    }

    private static Map<String, Object> asMap(PreparedStatement statement) throws SQLException {
        Map<String, Object> result = new HashMap<>(0);
        ResultSet rs = statement.getResultSet();
        if (rs.next()) {
            Node node = (Node) rs.getObject("lock");
            result = new HashMap<>(3);
            result.put("ip", node.getProperty("ip"));
            result.put("date", node.getProperty("date"));
        }
        return result;
    }

    private static void registerDeleteLockShutDownHook(UnlockTask unlockTask) {
        Runtime runtime = Runtime.getRuntime();
        Thread thread = new Thread(unlockTask);
        try {
            runtime.addShutdownHook(thread);
        }
        catch (IllegalArgumentException ignored) {
        }
    }

}
