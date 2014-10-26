package com.liquigraph.core.validation;

import com.google.common.collect.Lists;
import com.liquigraph.core.model.Changeset;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static com.google.common.collect.Lists.newArrayList;
import static com.liquigraph.core.model.Checksums.checksum;
import static java.lang.String.format;

public class PersistedChangesetValidatorTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private PersistedChangesetValidator validator = new PersistedChangesetValidator();

    @Test
    public void passes_if_nothing_persisted_yet() {
        validator.validate(
            newArrayList(changeset("identifier", "author", "MATCH m RETURN m")),
            Lists.<Changeset>newArrayList()
        );
    }

    @Test
    public void passes_if_all_existing_changesets_have_not_changed_checksum() {
        validator.validate(
            newArrayList(changeset("identifier", "author", "MATCH m RETURN m")),
            newArrayList(changeset("identifier", "author2", "MATCH m RETURN m"))
        );
    }

    @Test
    public void passes_if_changesets_have_modified_checksums_but_run_on_change() {
        validator.validate(
            newArrayList(changeset("identifier", "author", "MATCH m2 RETURN m2", true)),
            newArrayList(changeset("identifier", "author", "MATCH m RETURN m"))
        );
    }

    @Test
    public void fails_if_changesets_with_same_id_have_different_checksums() throws Exception {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Changeset with ID <identifier> has conflicted checksums.");
        thrown.expectMessage(format(" - Declared: <%s>", checksum("MATCH m RETURN m")));
        thrown.expectMessage(format(" - Persisted: <%s>", checksum("MATCH (m)-->(z) RETURN m, z")));

        validator.validate(
            newArrayList(changeset("identifier", "author", "MATCH m RETURN m")),
            newArrayList(changeset("identifier", "author2", "MATCH (m)-->(z) RETURN m, z"))
        );
    }

    @Test
    public void fails_if_less_declared_changesets_than_persisted_ones() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("At least 1 declared changeset(s) is/are missing.");

        validator.validate(
            newArrayList(changeset("identifier", "author", "MATCH m RETURN m")),
            newArrayList(
                changeset("identifier", "author", "MATCH m RETURN m"),
                changeset("identifier2", "author", "MATCH n RETURN n")
            )
        );
    }

    @Test
    public void fails_if_order_of_changelogs_is_different() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Declared changeset number 1 should have");
        thrown.expectMessage("ID:\t <identifier>");
        thrown.expectMessage("Found:\t<identifier2>.");
        thrown.expectMessage("Declared changeset number 2 should have");
        thrown.expectMessage("ID:\t <identifier2>");
        thrown.expectMessage("Found:\t<identifier>.");

        validator.validate(
            newArrayList(
                changeset("identifier2", "author", "MATCH n RETURN n"),
                changeset("identifier", "author", "MATCH m RETURN m")
            ),
            newArrayList(
                changeset("identifier", "author", "MATCH m RETURN m"),
                changeset("identifier2", "author", "MATCH n RETURN n")
            )
        );
    }

    private Changeset changeset(String identifier, String author, String query, boolean runOnChange) {
        Changeset changeset = changeset(identifier, author, query);
        changeset.setRunOnChange(true);
        return changeset;
    }

    private Changeset changeset(String id, String author, String query) {
        Changeset changeset = new Changeset();
        changeset.setId(id);
        changeset.setAuthor(author);
        changeset.setQuery(query);
        return changeset;
    }
}