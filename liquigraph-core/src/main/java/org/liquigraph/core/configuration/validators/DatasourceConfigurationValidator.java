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

import javax.sql.DataSource;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Optional;

import static java.lang.String.format;
import static java.util.Collections.emptyList;

public class DatasourceConfigurationValidator {

    public Collection<String> validate(Optional<String> uri, Optional<DataSource> dataSource) {
        if (uri.isPresent() == dataSource.isPresent()) {
            return Collections.singletonList("Exactly one of JDBC URI or DataSource need to be configured");
        }
        return uri.map(DatasourceConfigurationValidator::validateConnectionString).orElse(emptyList());
    }

    private static Collection<String> validateConnectionString(String uri) {
        Collection<String> errors = new LinkedList<>();
        if (!uri.startsWith("jdbc:neo4j:http") && !uri.startsWith("jdbc:neo4j:bolt")) {
            errors.add(format("Invalid JDBC URI. Supported configurations:%n" +
                "\t - jdbc:neo4j:http(s)://<host>:<port>/%n" +
                "\t - jdbc:neo4j:bolt://<host>:<port>/%n" +
                "Given: %s", uri
            ));
        }
        return errors;
    }
}
