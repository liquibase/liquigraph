package org.liquigraph.core.parser;

import com.google.common.collect.Lists;
import org.assertj.core.api.iterable.Extractor;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.liquigraph.core.model.*;
import org.liquigraph.core.validation.XmlSchemaValidator;
import org.w3c.dom.Node;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;
import static org.liquigraph.core.model.PreconditionErrorPolicy.FAIL;
import static org.liquigraph.core.model.PreconditionErrorPolicy.MARK_AS_EXECUTED;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ChangelogParserTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private XmlSchemaValidator validator = new XmlSchemaValidator();
    private final ChangelogPreprocessor preprocessor = new ChangelogPreprocessor(new ImportResolver());
    private final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

    private final ChangelogParser parser = new ChangelogParser(validator, preprocessor);

    @Test
    public void parses_single_changelog() {
        Collection<Changeset> changesets = parser.parse(classLoader, "changelog/changelog.xml");

        assertThat(changesets)
            .extracting("author", "id", "query")
            .containsExactly(
                tuple("fbiville", "first-changelog", "MATCH (n) RETURN n"),
                tuple("team", "second-changelog", "MATCH (m) RETURN m")
            );
    }

    @Test
    public void parses_changelog_of_changelogs() {
        Collection<Changeset> changesets = parser.parse(classLoader, "changelog/changelog-of-changelogs.xml");

        assertThat(changesets)
            .extracting("author", "id", "query")
            .containsExactly(
                tuple("fbiville", "first-changelog", "MATCH (n) RETURN n"),
                tuple("team", "second-changelog", "MATCH (m) RETURN m"),
                tuple("company", "third-changelog", "MATCH (l) RETURN l")
            );
    }

    @Test
    @SuppressWarnings("unchecked")
    public void parses_changelog_with_execution_contexts() {
        Collection<Changeset> changesets = parser.parse(classLoader, "changelog/changelog-with-execution-contexts.xml");

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
        Collection<Changeset> changesets = parser.parse(classLoader, "changelog/changelog-with-run-modes.xml");

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
        Collection<Changeset> changesets = parser.parse(classLoader, "changelog/changelog-with-preconditions.xml");

        assertThat(changesets)
            .extracting("id", "precondition")
            .containsExactly(
                tuple("first-changelog", precondition(FAIL, "MATCH (npre) RETURN npre")),
                tuple("second-changelog", precondition(MARK_AS_EXECUTED, "MATCH (mpre) RETURN mpre")),
                tuple("third-changelog", null)
            );
    }

    @Test
    public void parses_changelog_with_nested_preconditions() {
        Collection<Changeset> changesets = parser.parse(classLoader, "changelog/changelog-with-nested-preconditions.xml");

        assertThat(changesets).extracting("precondition.query.class")
            .containsExactly(
                SimpleQuery.class,
                AndQuery.class,
                OrQuery.class,
                OrQuery.class
            );
    }

    @Test
    public void forwards_validation_errors() throws Exception {
        given_validation_errors(asList("error1", "error2"));

        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage(String.format("%n\terror1%n\terror2"));

        parser.parse(classLoader, "changelog/changelog.xml");
    }

    // fragile: uses reflection
    private void given_validation_errors(List<String> errors) throws Exception {
        XmlSchemaValidator validator = mock(XmlSchemaValidator.class);
        when(validator.validateSchema(any(Node.class))).thenReturn(errors);

        Field field = ChangelogParser.class.getDeclaredField("validator");
        field.setAccessible(true);
        field.set(parser, validator);
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