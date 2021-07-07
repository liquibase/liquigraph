/*
 * Copyright 2014-2020 the original author or authors.
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
package org.liquigraph.testing;

import java.util.Collection;

import org.junit.After;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.slf4j.bridge.SLF4JBridgeHandler;

import static java.util.Arrays.asList;

@RunWith(Parameterized.class)
public abstract class ParameterizedDatabaseIT {

    private static final JdbcAwareGraphDatabase communityGraphDatabase;

    private static final JdbcAwareGraphDatabase enterpriseGraphDatabase;

    static {
        SLF4JBridgeHandler.removeHandlersForRootLogger();

        communityGraphDatabase = JdbcAwareGraphDatabase.createCommunityInstance();
        enterpriseGraphDatabase = JdbcAwareGraphDatabase.createEnterpriseInstance();
    }

    protected final JdbcAwareGraphDatabase graphDb;

    protected final String uri;

    @Parameters(name = "{index}: {0}")
    public static Collection<Object[]> graphDbProvider() {
        communityGraphDatabase.ensureStarted();
        enterpriseGraphDatabase.ensureStarted();

        return asList(new Object[][] {
//            {"Community Edition - Bolt", communityGraphDatabase, communityGraphDatabase.boltJdbcUrl()},
//            {"Community Edition - HTTP", communityGraphDatabase, communityGraphDatabase.httpJdbcUrl()},
            {"Enterprise Edition - Bolt", enterpriseGraphDatabase, enterpriseGraphDatabase.boltJdbcUrl()},
//            {"Enterprise Edition - HTTP", enterpriseGraphDatabase, enterpriseGraphDatabase.httpJdbcUrl()},
        });
    }

    public ParameterizedDatabaseIT(String ignored, JdbcAwareGraphDatabase graphDb, String uri) {
        this.graphDb = graphDb;
        this.uri = uri;
    }

    @After
    public void cleanUp() {
        communityGraphDatabase.cleanUp();
        enterpriseGraphDatabase.cleanUp();
    }
}
