package org.liquigraph.core.validation;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.w3c.dom.Node;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;

public class XmlSchemaValidatorTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private XmlSchemaValidator validator = new XmlSchemaValidator();

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

    private Node asNode(String path) throws Exception {
        try (InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream(path)) {
            return DocumentBuilderFactory.newInstance()
                .newDocumentBuilder()
                .parse(stream);
        }
    }
}