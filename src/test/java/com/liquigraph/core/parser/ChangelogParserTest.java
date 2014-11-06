package com.liquigraph.core.parser;

import com.google.common.collect.Lists;
import com.liquigraph.core.model.*;
import org.assertj.core.api.iterable.Extractor;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Collection;

import static com.google.common.collect.Lists.newArrayList;
import static com.liquigraph.core.model.PreconditionErrorPolicy.FAIL;
import static com.liquigraph.core.model.PreconditionErrorPolicy.MARK_AS_EXECUTED;
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
                Lists.<String>newArrayList()
            );
    }

    @Test
    public void parses_changelog_with_run_always_and_run_on_change_attributes() {
        Collection<Changeset> changesets = parser.parse("/changelog-with-run-modes.xml");

        assertThat(changesets)
            .extracting("id", "runAlways", "runOnChange")
            .containsExactly(
                tuple("first-changelog", true, true),
                tuple("second-changelog", false, true),
                tuple("third-changelog", true, false),
                tuple("fourth-changelog", false, false)
            );
    }

    @Test
    public void parses_changelog_with_preconditions() {
        Collection<Changeset> changesets = parser.parse("/changelog-with-preconditions.xml");

        assertThat(changesets)
            .extracting("id", "precondition")
            .containsExactly(
                tuple("first-changelog", precondition(FAIL, "MATCH npre RETURN npre")),
                tuple("second-changelog", precondition(MARK_AS_EXECUTED, "MATCH mpre RETURN mpre")),
                tuple("third-changelog", null)
            );
    }

    @Test
    public void parses_changelog_with_nested_preconditions() {
        Collection<Changeset> changesets = parser.parse("/changelog-with-nested-preconditions.xml");

        assertThat(changesets).extracting("precondition.query.class")
            .containsExactly(
                SimpleQuery.class,
                AndQuery.class,
                OrQuery.class,
                OrQuery.class
            );
    }

    private Precondition precondition(PreconditionErrorPolicy errorPolicy, String query) {
        Precondition precondition = new Precondition();
        precondition.setPolicy(errorPolicy);
        SimpleQuery simpleQuery = new SimpleQuery();
        simpleQuery.setQuery(query);
        precondition.setQuery(simpleQuery);
        return precondition;
    }
}