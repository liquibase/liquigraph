/**
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

import org.junit.Test;

import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;

public class MandatoryOptionValidatorTest {

    private MandatoryOptionValidator validator = new MandatoryOptionValidator();

    private ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

    @Test
    public void fails_on_invalid_jdbc_uri() {
        Collection<String> errors = validator.validate(
            classLoader,
            "changelog/changelog.xml",
            "foo"
        );

        assertThat(errors).containsExactly(String.format(
            "Invalid JDBC URI. Supported configurations:%n" +
            "\t - jdbc:neo4j:http://<host>:<port>/%n" +
            "\t - jdbc:neo4j:bolt://<host>:<port>/%n" +
            "Given: foo"
        ));
    }

    @Test
    public void fails_on_incomplete_graph_instance_configuration() {
        Collection<String> errors = validator.validate(
            classLoader,
            "changelog/changelog.xml",
            null
        );

        assertThat(errors).containsExactly(
            "'uri' should not be null"
        );
    }
}
