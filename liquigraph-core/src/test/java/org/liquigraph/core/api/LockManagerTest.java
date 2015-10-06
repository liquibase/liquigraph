package org.liquigraph.core.api;

import com.google.common.base.Throwables;
import org.junit.Rule;
import org.junit.Test;
import org.liquigraph.core.configuration.Configuration;
import org.liquigraph.core.configuration.ConfigurationBuilder;
import org.liquigraph.core.rules.EmbeddedGraphDatabaseRule;
import org.liquigraph.core.writer.GraphJdbcConnector;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static com.google.common.base.Throwables.propagate;
import static org.assertj.core.api.Assertions.assertThat;

public class LockManagerTest {

    @Rule
    public final EmbeddedGraphDatabaseRule graph = new EmbeddedGraphDatabaseRule("neo");

    private final LockManager lockManager = new LockManager(new GraphJdbcConnector());
    private final Configuration configuration = new ConfigurationBuilder()
        .withUri(graph.uri())
        .withClassLoader(Thread.currentThread().getContextClassLoader())
        // does not matter for this test
        .withMasterChangelogLocation("changelog/empty-changelog.xml")
        .build();

    @Test
    public void ensures_only_one_execution_at_a_time() throws Exception {
        CountDownLatch startSignal = new CountDownLatch(1);
        int threadCount = 10;
        CountDownLatch doneSignal = new CountDownLatch(threadCount);
        for (int i = 0; i < threadCount; i++) {
            new Thread(
                new LockWorker(
                    startSignal,
                    doneSignal,
                    configuration,
                    lockManager
                )
            ).start();
        }
        startSignal.countDown();
        doneSignal.await(5, TimeUnit.SECONDS);

        Connection connection = new GraphJdbcConnector().connect(configuration);
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("MATCH (lock:__LiquigraphLock) RETURN lock")) {

            assertThat(resultSet.next()).isFalse();
        }

    }

    private static class LockWorker implements Runnable {

        private final CountDownLatch startSignal;
        private final CountDownLatch doneSignal;
        private final LockManager lockManager;
        private final Configuration configuration;

        public LockWorker(CountDownLatch startSignal,
                          CountDownLatch doneSignal,
                          Configuration configuration,
                          LockManager lockManager) {

            this.startSignal = startSignal;
            this.doneSignal = doneSignal;
            this.lockManager = lockManager;
            this.configuration = configuration;
        }

        @Override
        public void run() {
            try {
                startSignal.await(5, TimeUnit.SECONDS);
                lockManager.lockAndExecute(new LongRunningTask(), configuration);
                doneSignal.countDown();
            } catch (InterruptedException e) {
                throw Throwables.propagate(e);
            }
        }
    }

    private static class LongRunningTask implements Runnable {

        @Override
        public void run() {
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                throw propagate(e);
            }
        }
    }
}
