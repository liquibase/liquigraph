package com.liquigraph.core.validation;

import com.liquigraph.core.model.Changeset;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static com.google.common.collect.Lists.newArrayList;

public class DeclaredChangesetValidatorTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private DeclaredChangesetValidator validator = new DeclaredChangesetValidator();

    @Test
    public void fails_with_missing_attributes() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Changeset 1 - 'id' should not be missing/blank.");
        thrown.expectMessage("Changeset 1 - 'author' should not be missing/blank.");
        thrown.expectMessage("Changeset 1 - 'query' should not be missing/blank.");

        validator.validate(newArrayList(new Changeset()));
    }

    @Test
    public void fails_with_repeated_changeset_ids() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("<identifier> is/are declared more than once.");

        validator.validate(newArrayList(
            changeset("identifier", "author", "MATCH n RETURN n"),
            changeset("identifier", "author2", "MATCH m RETURN m")
        ));
    }

    private Changeset changeset(String id, String author, String query) {
        Changeset changeset = new Changeset();
        changeset.setId(id);
        changeset.setAuthor(author);
        changeset.setQuery(query);
        return changeset;
    }
}