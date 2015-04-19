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

        assertThat(errors).containsExactly(
            "Invalid JDBC URI. Supported configurations:\n" +
            "\t - jdbc:neo4j://<host>:<port>/\n" +
            "\t - jdbc:neo4j:file:/path/to/db\n" +
            "\t - jdbc:neo4j:mem or jdbc:neo4j:mem:name.\n" +
            "Given: foo"
        );
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