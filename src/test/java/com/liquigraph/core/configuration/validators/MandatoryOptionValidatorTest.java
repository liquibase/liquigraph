package com.liquigraph.core.configuration.validators;

import com.liquigraph.core.rules.EmbeddedGraphDatabaseRule;
import org.junit.Rule;
import org.junit.Test;

import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;

public class MandatoryOptionValidatorTest {

    @Rule
    public EmbeddedGraphDatabaseRule graphDatabase = new EmbeddedGraphDatabaseRule();
    private MandatoryOptionValidator validator = new MandatoryOptionValidator();

    @Test
    public void fails_on_ambiguous_graph_instance_configuration() {
        Collection<String> errors = validator.validate(
            "/changelog.xml",
            graphDatabase.graphDirectory().toURI().toString(),
            graphDatabase.graphDatabase()
        );

        assertThat(errors).containsExactly(
            "Ambiguous or incomplete configuration. " +
            "Either 'uri' OR 'graphDatabaseService' should be specified."
        );
    }

    @Test
    public void fails_on_incomplete_graph_instance_configuration() {
        Collection<String> errors = validator.validate(
            "/changelog.xml",
            null,
            null
        );

        assertThat(errors).containsExactly(
            "Ambiguous or incomplete configuration. " +
            "Either 'uri' OR 'graphDatabaseService' should be specified.",
            "'uri' should not be null"
        );
    }
}