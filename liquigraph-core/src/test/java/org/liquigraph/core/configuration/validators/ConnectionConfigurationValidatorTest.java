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
package org.liquigraph.core.configuration.validators;

import com.google.common.base.Optional;
import org.junit.Test;
import org.neo4j.graphdb.GraphDatabaseService;

import javax.sql.DataSource;
import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class ConnectionConfigurationValidatorTest {

    private ConnectionConfigurationValidator connectionConfigurationValidator = new ConnectionConfigurationValidator();

    @Test
    public void validates_proper_configuration_by_uri() {
        Collection<String> errors = connectionConfigurationValidator.validate(
            Optional.of("jdbc:neo4j:mem:db"),
            Optional.<DataSource>absent(),
            Optional.<GraphDatabaseService>absent());

        assertThat(errors).isEmpty();
    }

    @Test
    public void validates_proper_configuration_by_datasource() {
        Collection<String> errors = connectionConfigurationValidator.validate(
            Optional.<String>absent(),
            Optional.of(mock(DataSource.class)),
            Optional.<GraphDatabaseService>absent());

        assertThat(errors).isEmpty();
    }

    @Test
    public void validates_proper_configuration_by_database() {
        Collection<String> errors = connectionConfigurationValidator.validate(
                Optional.<String>absent(),
                Optional.<DataSource>absent(),
                Optional.of(mock(GraphDatabaseService.class)));

        assertThat(errors).isEmpty();
    }

    @Test
    public void returns_error_when_two_or_more_configurations_are_provided() {
        Collection<String> errors = connectionConfigurationValidator.validate(
                Optional.of("jdbc:neo4j:mem:db"),
                Optional.of(mock(DataSource.class)),
                Optional.of(mock(GraphDatabaseService.class)));

        assertThat(errors).containsExactly("Exactly one of JDBC URI, JDBC DataSource or GraphDatabaseService needs to be configured");
    }

    @Test
    public void returns_error_if_no_configuration_options_provided() {
        Collection<String> errors = connectionConfigurationValidator.validate(
                Optional.<String>absent(),
                Optional.<DataSource>absent(),
                Optional.<GraphDatabaseService>absent());

        assertThat(errors).containsExactly("Exactly one of JDBC URI, JDBC DataSource or GraphDatabaseService needs to be configured");
    }

    @Test
    public void returns_error_if_uri_is_invalid() {
        Collection<String> errors = connectionConfigurationValidator.validate(
                Optional.of("not:a:valid:jdbc:uri"),
                Optional.<DataSource>absent(),
                Optional.<GraphDatabaseService>absent());

        assertThat(errors).containsExactly(
            "Invalid JDBC URI. Supported configurations:\n" +
            "\t - jdbc:neo4j://<host>:<port>/\n" +
            "\t - jdbc:neo4j:file:/path/to/db\n" +
            "\t - jdbc:neo4j:mem or jdbc:neo4j:mem:name.\n" +
            "Given: not:a:valid:jdbc:uri");

    }
}