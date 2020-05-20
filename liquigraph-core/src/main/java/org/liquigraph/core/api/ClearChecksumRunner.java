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
package org.liquigraph.core.api;

import org.liquigraph.core.configuration.Configuration;
import org.liquigraph.core.io.ClearChecksumWriter;
import org.liquigraph.core.io.GraphJdbcConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;

public class ClearChecksumRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClearChecksumRunner.class);

    public void run(Configuration configuration) {
        GraphJdbcConnector graphJdbcConnector = new GraphJdbcConnector(configuration);
        try (Connection writeConnection = graphJdbcConnector.connect()) {
            new ClearChecksumWriter(writeConnection).write();
        } catch (SQLException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }
}
