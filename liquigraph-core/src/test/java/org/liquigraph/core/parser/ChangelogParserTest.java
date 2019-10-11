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
package org.liquigraph.core.parser;

import org.junit.Test;
import org.liquigraph.core.io.xml.ChangelogLoader;
import org.liquigraph.core.io.xml.ChangelogParser;
import org.liquigraph.core.io.xml.ChangelogPreprocessor;
import org.liquigraph.core.io.xml.ClassLoaderChangelogLoader;
import org.liquigraph.core.io.xml.ImportResolver;
import org.liquigraph.core.io.xml.XmlSchemaValidator;
import org.liquigraph.core.model.AndConditionQuery;
import org.liquigraph.core.model.Changeset;
import org.liquigraph.core.model.OrConditionQuery;
import org.liquigraph.core.model.ParameterizedQuery;
import org.liquigraph.core.model.Precondition;
import org.liquigraph.core.model.PreconditionErrorPolicy;
import org.liquigraph.core.model.Query;
import org.liquigraph.core.model.SimpleConditionQuery;
import org.liquigraph.core.model.SimpleQuery;
import org.w3c.dom.Node;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.groups.Tuple.tuple;
import static org.liquigraph.core.model.Checksums.checksum;
import static org.liquigraph.core.model.PreconditionErrorPolicy.FAIL;
import static org.liquigraph.core.model.PreconditionErrorPolicy.MARK_AS_EXECUTED;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ChangelogParserTest {

    private final XmlSchemaValidator validator = new XmlSchemaValidator();
    private final ChangelogPreprocessor preprocessor = new ChangelogPreprocessor(new ImportResolver());
    private final ChangelogLoader changelogLoader = ClassLoaderChangelogLoader.currentThreadContextClassLoader();

    private final ChangelogParser parser = new ChangelogParser(validator, preprocessor);

    @Test
    public void parses_single_changelog() {
        Collection<Changeset> changesets = parser.parse(changelogLoader, "changelog/changelog.xml");

        assertThat(changesets)
            .extracting("author", "id", "queries")
            .containsExactly(
                tuple("fbiville", "first-changelog", singletonList(new SimpleQuery("MATCH (n) RETURN n"))),
                tuple("team", "second-changelog", singletonList(new SimpleQuery("MATCH (m) RETURN m")))
            );
    }

    @Test
    public void parses_changelog_of_changelogs() {
        Collection<Changeset> changesets = parser.parse(changelogLoader, "changelog/changelog-of-changelogs.xml");

        assertThat(changesets)
            .extracting("author", "id", "queries")
            .containsExactly(
                tuple("fbiville", "first-changelog", singletonList(new SimpleQuery("MATCH (n) RETURN n"))),
                tuple("team", "second-changelog", singletonList(new SimpleQuery("MATCH (m) RETURN m"))),
                tuple("company", "third-changelog", singletonList(new SimpleQuery("MATCH (l) RETURN l")))
            );
    }

    @Test
    @SuppressWarnings("unchecked")
    public void parses_changelog_with_execution_contexts() {
        Collection<Changeset> changesets = parser.parse(changelogLoader, "changelog/changelog-with-execution-contexts.xml");

        assertThat(changesets)
            .extracting(Changeset::getExecutionsContexts)
            .containsExactly(
                Arrays.asList("foo", "bar"),
                singletonList(("baz")),
                Collections.emptyList()
            );
    }

    @Test
    public void parses_changelog_with_run_always_and_run_on_change_attributes() {
        Collection<Changeset> changesets = parser.parse(changelogLoader, "changelog/changelog-with-run-modes.xml");

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
        Collection<Changeset> changesets = parser.parse(changelogLoader, "changelog/changelog-with-preconditions.xml");

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
        Collection<Changeset> changesets = parser.parse(changelogLoader, "changelog/changelog-with-nested-preconditions.xml");

        assertThat(changesets).extracting("precondition.query.class")
            .containsExactly(
                SimpleConditionQuery.class,
                AndConditionQuery.class,
                OrConditionQuery.class,
                OrConditionQuery.class
            );
    }

    @Test
    public void forwards_validation_errors() throws Exception {
        XmlSchemaValidator validator = mock(XmlSchemaValidator.class);
        when(validator.validateSchema(any(Node.class))).thenReturn(asList("error1", "error2"));
        ChangelogParser parser = new ChangelogParser(validator, preprocessor);

        assertThatThrownBy(() -> parser.parse(changelogLoader, "changelog/changelog.xml"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage(String.format("%n\terror1%n\terror2"));
    }

    @Test
    public void populates_checksum() {
        Collection<Changeset> changesets = parser.parse(changelogLoader, "changelog/changelog.xml");

        SimpleQuery expectedQuery1 = new SimpleQuery("MATCH (n) RETURN n");
        SimpleQuery expectedQuery2 = new SimpleQuery("MATCH (m) RETURN m");
        assertThat(changesets)
            .extracting("queries", "checksum")
            .containsExactly(
                tuple(singletonList(expectedQuery1), checksum(singletonList(expectedQuery1))),
                tuple(singletonList(expectedQuery2), checksum(singletonList(expectedQuery2)))
            );
    }

    @Test
    public void parses_changesets_with_several_queries() {
        Collection<Changeset> changesets = parser.parse(changelogLoader, "changelog/multiple_queries/changelog.xml");

        assertThat(changesets)
            .hasSize(1)
            .extracting(Changeset::getQueries)
            .flatExtracting(queries -> queries.stream().map(Query::getQuery).collect(toList()))
            .containsExactly("MATCH (n) RETURN n", "MATCH (m) RETURN m");
    }

    @Test
    public void should_also_support_simple_parameterized_queries() {
        Collection<Changeset> changesets = parser.parse(changelogLoader, "changelog/parameterized_queries/changelog.xml");

        assertThat(changesets).hasSize(1);
        assertThat(changesets.iterator().next().getQueries())
            .containsExactly(
                new SimpleQuery("CREATE (n:Person) RETURN n"),
                new ParameterizedQuery("MATCH (n:Person) SET n.name = {1} RETURN n", singletonList("some name")),
                new SimpleQuery("CREATE (o:Place) RETURN o"),
                new ParameterizedQuery("MATCH (o:Place) SET o.name = {1}, o.city = {2} RETURN o", Arrays.<String>asList("some other name", "some place")));
    }

    private Precondition precondition(PreconditionErrorPolicy errorPolicy, String query) {
        Precondition precondition = new Precondition();
        precondition.setPolicy(errorPolicy);
        SimpleConditionQuery simpleQuery = new SimpleConditionQuery();
        simpleQuery.setQuery(query);
        precondition.setQuery(simpleQuery);
        return precondition;
    }
}
