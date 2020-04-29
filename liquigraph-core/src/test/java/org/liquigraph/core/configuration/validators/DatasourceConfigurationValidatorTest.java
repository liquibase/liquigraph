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

import org.junit.Test;

import javax.sql.DataSource;
import java.util.Collection;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class DatasourceConfigurationValidatorTest {

    private final DatasourceConfigurationValidator datasourceConfigurationValidator = new DatasourceConfigurationValidator();

    @Test
    public void validates_proper_configuration_by_bolt_uri() {
        Collection<String> errors = datasourceConfigurationValidator.validate(
            Optional.of("jdbc:neo4j:bolt://localhost:666"),
            Optional.empty(),
            Optional.empty());

        assertThat(errors).isEmpty();
    }

    @Test
    public void validates_proper_configuration_by_http_uri() {
        Collection<String> errors = datasourceConfigurationValidator.validate(
        Optional.of("jdbc:neo4j:http://localhost:666"),
        Optional.empty(),
        Optional.empty());

        assertThat(errors).isEmpty();
    }

    @Test
    public void validates_proper_configuration_by_https_uri() {
        Collection<String> errors = datasourceConfigurationValidator.validate(
        Optional.of("jdbc:neo4j:https://localhost:666"),
        Optional.empty(),
        Optional.empty());

        assertThat(errors).isEmpty();
    }

    @Test
    public void validates_proper_configuration_by_neo4j_uri() {
        Collection<String> errors = datasourceConfigurationValidator.validate(
        Optional.of("jdbc:neo4j:neo4j://localhost:666"),
        Optional.empty(),
        Optional.empty());

        assertThat(errors).isEmpty();
    }

    @Test
    public void validates_proper_configuration_by_datasource() {
        Collection<String> errors = datasourceConfigurationValidator.validate(
            Optional.empty(),
            Optional.of(mock(DataSource.class)),
            Optional.empty());

        assertThat(errors).isEmpty();
    }

    @Test
    public void returns_error_if_both_uri_and_datasource_are_provided() {
        Collection<String> errors = datasourceConfigurationValidator.validate(
            Optional.of("jdbc:neo4j:bolt://localhost:666"),
            Optional.of(mock(DataSource.class)),
            Optional.empty());

        assertThat(errors).containsExactly("Exactly one of JDBC URI or DataSource need to be configured");
    }

    @Test
    public void returns_error_if_both_uri_and_datasource_are_not_provided() {
        Collection<String> errors = datasourceConfigurationValidator.validate(
            Optional.empty(),
            Optional.empty(),
            Optional.empty());

        assertThat(errors).containsExactly("Exactly one of JDBC URI or DataSource need to be configured");
    }

    @Test
    public void returns_error_if_both_database_instance_and_datasource_are_provided() {
        Collection<String> errors = datasourceConfigurationValidator.validate(
            Optional.empty(),
            Optional.of(mock(DataSource.class)),
            Optional.of("some-instance"));

        assertThat(errors).containsExactly("Database instance cannot be configured when configuring a DataSource");
    }

    @Test
    public void returns_error_if_uri_is_invalid() {
        Collection<String> errors = datasourceConfigurationValidator.validate(
            Optional.of("not:a:valid:jdbc:uri"),
            Optional.empty(),
            Optional.empty());

        assertThat(errors).containsExactly(String.format(
            "Invalid JDBC URI. Supported configurations:%n" +
                "\t - jdbc:neo4j:http(s)://<host>:<port>/%n" +
                "\t - jdbc:neo4j:bolt://<host>:<port>/%n" +
                "\t - jdbc:neo4j:neo4j://<host>:<port>/%n" +
                "Given: not:a:valid:jdbc:uri"
        ));
    }
}
