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
package org.liquigraph.core.validation;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.liquigraph.core.io.xml.XmlSchemaValidator;
import org.liquigraph.core.model.Changeset;
import org.w3c.dom.Node;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.util.Collection;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

public class XmlSchemaValidatorTest {

    @Rule public ExpectedException thrown = ExpectedException.none();
    private final XmlSchemaValidator validator = new XmlSchemaValidator();

    @Before
    public void setUp(){
        Locale.setDefault(Locale.ENGLISH);
    }


    @Test
    public void parses_changelog_without_author() throws Exception {
        Collection<String> errors = validator.validateSchema(
            asNode("changelog/invalid_changesets/changelog-without-author.xml")
        );

        assertThat(errors)
            .containsExactly("cvc-complex-type.4: Attribute 'author' must appear on element 'changeset'.");
    }

    @Test
    public void parses_changelog_without_id() throws Exception {
        Collection<String> errors = validator.validateSchema(
            asNode("changelog/invalid_changesets/changelog-without-id.xml")
        );

        assertThat(errors)
            .containsExactly("cvc-complex-type.4: Attribute 'id' must appear on element 'changeset'.");
    }

    @Test
    public void parses_changelog_without_query() throws Exception {
        Collection<String> errors = validator.validateSchema(
            asNode("changelog/invalid_changesets/changelog-without-query.xml")
        );

        assertThat(errors)
            .containsExactly(
                "cvc-complex-type.2.4.b: The content of element 'changeset' is not complete. " +
                "One of '{precondition, query}' is expected.");
    }

    @Test
    public void parses_changelog_with_invalid_precondition() throws Exception {
        Collection<String> errors = validator.validateSchema(
            asNode("changelog/invalid_changesets/changelog-with-invalid-precondition-query.xml")
        );

        assertThat(errors)
            .containsExactly(
                "cvc-complex-type.2.4.a: Invalid content was found starting with element 'toto'. " +
                "One of '{and, or, query}' is expected.");
    }

    @Test
    public void parses_changelog_with_duplicate_ids() throws Exception {
        Collection<String> errors = validator.validateSchema(
            asNode("changelog/invalid_changesets/changelog-with-duplicate-ids.xml")
        );

        assertThat(errors)
            .containsExactly(
                "Duplicate unique value [changelog] declared for identity constraint of element \"changelog\".");
    }

    @Test
    public void parses_changelog_with_explicit_schema() throws Exception {
        assertThat(validator.validateSchema(asNode("changelog/changelog-with-old-RC3-schema-location.xml"))).isEmpty();
        assertThat(validator.validateSchema(asNode("changelog/changelog-with-old-schema-location.xml"))).isEmpty();
        assertThat(validator.validateSchema(asNode("changelog/changelog-with-RC3-schema-location.xml"))).isEmpty();
        assertThat(validator.validateSchema(asNode("changelog/changelog-with-schema-location.xml"))).isEmpty();
    }

    private Node asNode(String path) throws Exception {
        try (InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream(path)) {
            return DocumentBuilderFactory.newInstance()
                .newDocumentBuilder()
                .parse(stream);
        }
    }
}
