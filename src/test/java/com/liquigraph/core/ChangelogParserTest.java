package com.liquigraph.core;

import com.liquigraph.core.model.Changeset;
import com.liquigraph.core.parser.ChangelogParser;
import org.assertj.core.api.iterable.Extractor;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Collection;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;

public class ChangelogParserTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private ChangelogParser parser = new ChangelogParser();

    @Test
    public void parses_single_changelog() {
        Collection<Changeset> changesets = parser.parse("/changelog.xml");

        assertThat(changesets)
            .extracting("author", "id", "query")
            .containsExactly(
                tuple("fbiville", "first-changelog", "MATCH n RETURN n"),
                tuple("team", "second-changelog", "MATCH m RETURN m")
            );
    }

    @Test
    @Ignore("not supported atm")
    public void parses_changelog_of_changelogs() {
        Collection<Changeset> changesets = parser.parse("/changelog-of-changelogs.xml");

        assertThat(changesets)
            .extracting("author", "id", "query")
            .containsExactly(
                tuple("fbiville", "first-changelog", "MATCH n RETURN n"),
                tuple("team", "second-changelog", "MATCH m RETURN m")
            );
    }

    @Test
    public void parses_changelog_with_execution_contexts() {
        Collection<Changeset> changesets = parser.parse("/changelog-with-execution-contexts.xml");

        assertThat(changesets)
            .extracting(new Extractor<Changeset, Collection<String>>() {
                @Override
                public Collection<String> extract(Changeset input) {
                    return input.getExecutionsContexts();
                }
            })
            .containsExactly(
                newArrayList("foo", "bar"),
                newArrayList("baz"),
                null
            );
    }
}