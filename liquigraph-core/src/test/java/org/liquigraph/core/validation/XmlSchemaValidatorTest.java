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

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.liquigraph.core.io.xml.XmlSchemaValidator;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.io.StringReader;
import java.util.Collection;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

public class XmlSchemaValidatorTest {

    private static final Locale initialLocale = Locale.getDefault();

    @Rule public ExpectedException thrown = ExpectedException.none();

    private final XmlSchemaValidator validator = new XmlSchemaValidator();

    @Before
    public void prepare(){
        Locale.setDefault(Locale.ENGLISH);
    }

    @After
    public void reset() {
        Locale.setDefault(initialLocale);
    }

    @Test
    public void validates_empty_changelog() throws Exception {
        assertThat(validator.validateSchema(contentsAsNode("<changelog />")))
            .isEmpty();
    }

    @Test
    public void invalidates_changelog_without_author() throws Exception {
        Collection<String> errors = validator.validateSchema(
            fileAsNode("changelog/invalid_changesets/changelog-without-author.xml")
        );

        assertThat(errors)
            .containsExactly("cvc-complex-type.4: Attribute 'author' must appear on element 'changeset'.");
    }

    @Test
    public void invalidates_changelog_without_id() throws Exception {
        Collection<String> errors = validator.validateSchema(
            fileAsNode("changelog/invalid_changesets/changelog-without-id.xml")
        );

        assertThat(errors)
            .containsExactly("cvc-complex-type.4: Attribute 'id' must appear on element 'changeset'.");
    }

    @Test
    public void invalidates_changelog_without_query() throws Exception {
        Collection<String> errors = validator.validateSchema(
            fileAsNode("changelog/invalid_changesets/changelog-without-query.xml")
        );

        assertThat(errors)
            .containsExactly(
                "cvc-complex-type.2.4.b: The content of element 'changeset' is not complete. " +
                "One of '{precondition, query, parameterized-query}' is expected.");
    }

    @Test
    public void invalidates_changelog_with_empty_precondition() throws Exception {
        Collection<String> errors = validator.validateSchema(
            contentsAsNode(
                "<changelog>\n" +
                "    <changeset author=\"fbiville\" id=\"first-changelog\">\n" +
                "        <precondition if-not-met=\"FAIL\" />\n" +
                "        <query><![CDATA[MATCH (n) RETURN n]]></query>\n" +
                "    </changeset>\n" +
                "</changelog>")
        );

        assertThat(errors).containsExactly(
            "cvc-complex-type.2.4.b: The content of element 'precondition' is not complete. " +
            "One of '{and, or, query}' is expected.");
    }

    @Test
    public void invalidates_changelog_with_malformed_precondition() throws Exception {
        Collection<String> errors = validator.validateSchema(
            fileAsNode("changelog/invalid_changesets/changelog-with-invalid-precondition-query.xml")
        );

        assertThat(errors)
            .containsExactly(
                "cvc-complex-type.2.4.a: Invalid content was found starting with element 'toto'. " +
                "One of '{and, or, query}' is expected.");
    }

    @Test
    public void invalidates_changelog_with_empty_postcondition() throws Exception {
        Collection<String> errors = validator.validateSchema(
            contentsAsNode(
                "<changelog>\n" +
                "    <changeset author=\"fbiville\" id=\"first-changelog\">\n" +
                "        <query><![CDATA[MATCH (n) RETURN n]]></query>\n" +
                "        <postcondition />\n" +
                "    </changeset>\n" +
                "</changelog>")
        );

        assertThat(errors)
            .containsExactly(
                "cvc-complex-type.2.4.b: The content of element 'postcondition' is not complete. " +
                "One of '{and, or, query}' is expected.");
    }

    @Test
    public void invalidates_changelog_with_duplicate_ids() throws Exception {
        Collection<String> errors = validator.validateSchema(
            fileAsNode("changelog/invalid_changesets/changelog-with-duplicate-ids.xml")
        );

        assertThat(errors)
            .containsExactly(
                "Duplicate unique value [changelog] declared for identity constraint of element \"changelog\".");
    }

    @Test
    public void validates_changelog_with_explicit_schema() throws Exception {
        // TODO: have an offline copy of the old schema
        assertThat(validator.validateSchema(fileAsNode("changelog/changelog-with-old-RC3-schema-location.xml"))).isEmpty();
        assertThat(validator.validateSchema(fileAsNode("changelog/changelog-with-old-schema-location.xml"))).isEmpty();
        assertThat(validator.validateSchema(fileAsNode("changelog/changelog-with-RC3-schema-location.xml"))).isEmpty();
        assertThat(validator.validateSchema(fileAsNode("changelog/changelog-with-schema-location.xml"))).isEmpty();
    }

    @Test
    public void validates_changelog_with_parameterized_queries() throws Exception {
        assertThat(validator.validateSchema(fileAsNode("changelog/parameterized_queries/changelog.xml"))).isEmpty();
    }

    @Test
    public void invalidates_changelog_with_parameterless_parameterized_queries() throws Exception {
        assertThat(validator.validateSchema(fileAsNode("changelog/invalid_changesets/changelog-with-parameterless-parameterized-queries.xml")))
            .containsExactly("cvc-complex-type.2.4.b: The content of element 'parameterized-query' is not complete. One of '{parameter}' is expected.");
    }

    private Node fileAsNode(String path) throws Exception {
        try (InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream(path)) {
            return DocumentBuilderFactory.newInstance()
                .newDocumentBuilder()
                .parse(stream);
        }
    }

    private Node contentsAsNode(String contents) throws Exception {
        return DocumentBuilderFactory.newInstance()
            .newDocumentBuilder()
            .parse(new InputSource(new StringReader(contents)));
    }
}
