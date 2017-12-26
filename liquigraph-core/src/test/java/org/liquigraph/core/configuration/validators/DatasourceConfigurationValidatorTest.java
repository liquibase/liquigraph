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
package org.liquigraph.core.configuration.validators;

import com.google.common.base.Optional;
import org.junit.Test;

import javax.sql.DataSource;
import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class DatasourceConfigurationValidatorTest {

    private DatasourceConfigurationValidator datasourceConfigurationValidator = new DatasourceConfigurationValidator();

    @Test
    public void validates_proper_configuration_by_uri() {
        Collection<String> errors = datasourceConfigurationValidator.validate(
            Optional.of("jdbc:neo4j:bolt://localhost:666"),
            Optional.<DataSource>absent()
        );

        assertThat(errors).isEmpty();
    }
    @Test
    public void validates_proper_configuration_by_datasource() {
        Collection<String> errors = datasourceConfigurationValidator.validate(
            Optional.<String>absent(),
            Optional.of(mock(DataSource.class))
        );

        assertThat(errors).isEmpty();
    }

    @Test
    public void returns_error_if_both_uri_and_datasource_are_provided() {
        Collection<String> errors = datasourceConfigurationValidator.validate(
                Optional.of("jdbc:neo4j:bolt://localhost:666"),
                Optional.of(mock(DataSource.class))
        );

        assertThat(errors).containsExactly("Exactly one of JDBC URI or DataSource need to be configured");
    }

    @Test
    public void returns_error_if_both_uri_and_datasource_are_not_provided() {
        Collection<String> errors = datasourceConfigurationValidator.validate(
                Optional.<String>absent(),
                Optional.<DataSource>absent()
        );

        assertThat(errors).containsExactly("Exactly one of JDBC URI or DataSource need to be configured");
    }

    @Test
    public void returns_error_if_uri_is_invalid() {
        Collection<String> errors = datasourceConfigurationValidator.validate(
                Optional.of("not:a:valid:jdbc:uri"),
                Optional.<DataSource>absent()
        );

        assertThat(errors).containsExactly(String.format(
            "Invalid JDBC URI. Supported configurations:%n" +
                "\t - jdbc:neo4j:http(s)://<host>:<port>/%n" +
                "\t - jdbc:neo4j:bolt://<host>:<port>/%n" +
                "Given: not:a:valid:jdbc:uri"
        ));

    }
}