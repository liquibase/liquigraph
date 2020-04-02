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
package org.liquigraph.core.configuration.validators;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.jdbc.Driver;

import javax.sql.DataSource;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

import static com.google.common.base.Optional.presentInstances;
import static com.google.common.collect.Iterables.size;
import static java.lang.String.format;
import static java.util.Arrays.asList;

public class ConnectionConfigurationValidator {

    public Collection<String> validate(Optional<String> uri, Optional<DataSource> dataSource, Optional<GraphDatabaseService> database) {

        if (size(presentInstances(asList(uri, dataSource, database))) != 1) {
            return ImmutableList.of("Exactly one of JDBC URI, JDBC DataSource or GraphDatabaseService needs to be configured");
        }

        if (uri.isPresent()) {
            return validateConnectionString(uri.get());
        }
        return Collections.emptyList();
    }

    private static Collection<String> validateConnectionString(String uri) {
        Collection<String> errors = new LinkedList<>();
        if (!uri.startsWith(Driver.CON_PREFIX)) {
            errors.add(format("Invalid JDBC URI. Supported configurations:\n" +
                    "\t - jdbc:neo4j://<host>:<port>/\n" +
                    "\t - jdbc:neo4j:file:/path/to/db\n" +
                    "\t - jdbc:neo4j:mem or jdbc:neo4j:mem:name.\n" +
                    "Given: %s", uri
            ));
        }
        return errors;
    }
}
