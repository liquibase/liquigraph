package com.liquigraph.core.validation;

import com.liquigraph.core.model.Changeset;
import org.junit.Test;

import java.util.Collection;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;

public class DeclaredChangesetValidatorTest {

    private DeclaredChangesetValidator validator = new DeclaredChangesetValidator();

    @Test
    public void passes_if_changeset_is_valid() {
        Collection<String> errors = validator.validate(newArrayList(
            changeset("identifier", "author", "MATCH n RETURN n")
        ));

        assertThat(errors).isEmpty();
    }

    @Test
    public void fails_with_missing_attributes() {
        Collection<String> errors = validator.validate(newArrayList(new Changeset()));

        assertThat(errors).containsExactly(
            "Changeset 1 - 'id' should not be missing/blank.",
            "Changeset 1 - 'author' should not be missing/blank.",
            "Changeset 1 - 'query' should not be missing/blank."
        );
    }

    @Test
    public void fails_with_repeated_changeset_ids() {
        Collection<String> errors = validator.validate(newArrayList(
            changeset("identifier", "author", "MATCH n RETURN n"),
            changeset("identifier", "author2", "MATCH m RETURN m")
        ));

        assertThat(errors).containsExactly(
            "<identifier> is/are declared more than once."
        );
    }

    private Changeset changeset(String id, String author, String query) {
        Changeset changeset = new Changeset();
        changeset.setId(id);
        changeset.setAuthor(author);
        changeset.setQuery(query);
        return changeset;
    }
}