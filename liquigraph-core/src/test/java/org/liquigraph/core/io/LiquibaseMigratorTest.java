/*
 * Copyright 2014-2021 the original author or authors.
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
package org.liquigraph.core.io;

import liquibase.change.core.RawSQLChange;
import liquibase.changelog.ChangeSet;
import liquibase.ext.neo4j.changelog.Neo4jChangelogHistoryService;
import liquibase.precondition.core.AndPrecondition;
import liquibase.precondition.core.OrPrecondition;
import liquibase.precondition.core.PreconditionContainer;
import liquibase.precondition.core.PreconditionContainer.ErrorOption;
import liquibase.precondition.core.PreconditionContainer.FailOption;
import liquibase.precondition.core.SqlPrecondition;
import liquibase.serializer.ChangeLogSerializer;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.liquigraph.core.model.AndQuery;
import org.liquigraph.core.model.Changeset;
import org.liquigraph.core.model.OrQuery;
import org.liquigraph.core.model.Postcondition;
import org.liquigraph.core.model.Precondition;
import org.liquigraph.core.model.PreconditionErrorPolicy;
import org.liquigraph.core.model.Query;
import org.liquigraph.core.model.SimpleQuery;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Arrays.stream;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.liquigraph.core.io.LiquibaseChangeSetsMatcher.matchesChangeSets;
import static org.liquigraph.core.model.PreconditionErrorPolicy.MARK_AS_EXECUTED;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class LiquibaseMigratorTest {

    private final ChangelogParser parser = mock(ChangelogParser.class);

    private final ChangeLogSerializer fileWriter = mock(ChangeLogSerializer.class);

    private final ChangelogGraphReader graphReader = mock(ChangelogGraphReader.class);

    private final Neo4jChangelogHistoryService graphWriter = mock(Neo4jChangelogHistoryService.class);

    private final ChangelogLoader loader = mock(ChangelogLoader.class);

    private final LiquibaseMigrator migrator = new LiquibaseMigrator(parser, graphReader, fileWriter, graphWriter);

    @Rule
    public TemporaryFolder folder = TemporaryFolder.builder().build();

    @Test
    public void migrates_simple_change_set() throws IOException {
        Changeset liquigraphChangeSet = new Changeset();
        liquigraphChangeSet.setId("id");
        liquigraphChangeSet.setAuthor("author");
        liquigraphChangeSet.setQueries(singletonList("RETURN 42"));
        when(parser.parse(loader, "some-changelog.xml")).thenReturn(singletonList(liquigraphChangeSet));
        List<ChangeSet> expectedChangeSets = singletonList(changeSet("result.xml", "id", "author", false, false, "", "RETURN 42"));

        migrator.migrateDeclaredChangeSets("some-changelog.xml", Collections.emptyList(), folder.newFile("result.xml"), loader);

        verify(fileWriter).write(argThat(matchesChangeSets(expectedChangeSets)), any());
    }

    @Test
    public void migrates_always_running_change_set() throws IOException {
        Changeset liquigraphChangeSet = new Changeset();
        liquigraphChangeSet.setId("id");
        liquigraphChangeSet.setAuthor("author");
        liquigraphChangeSet.setQueries(singletonList("RETURN 42"));
        liquigraphChangeSet.setRunAlways(true);
        when(parser.parse(loader, "some-changelog.xml")).thenReturn(singletonList(liquigraphChangeSet));
        List<ChangeSet> expectedChangeSets = singletonList(changeSet("result.xml", "id", "author", true, false, "", "RETURN 42"));

        migrator.migrateDeclaredChangeSets("some-changelog.xml", Collections.emptyList(), folder.newFile("result.xml"), loader);

        verify(fileWriter).write(argThat(matchesChangeSets(expectedChangeSets)), any());
    }

    @Test
    public void migrates_running_on_change_change_set() throws IOException {
        Changeset liquigraphChangeSet = new Changeset();
        liquigraphChangeSet.setId("id");
        liquigraphChangeSet.setAuthor("author");
        liquigraphChangeSet.setQueries(singletonList("RETURN 42"));
        liquigraphChangeSet.setRunOnChange(true);
        when(parser.parse(loader, "some-changelog.xml")).thenReturn(singletonList(liquigraphChangeSet));
        List<ChangeSet> expectedChangeSets = singletonList(changeSet("result.xml", "id", "author", false, true, "", "RETURN 42"));

        migrator.migrateDeclaredChangeSets("some-changelog.xml", Collections.emptyList(), folder.newFile("result.xml"), loader);

        verify(fileWriter).write(argThat(matchesChangeSets(expectedChangeSets)), any());
    }

    @Test
    public void migrates_change_set_with_execution_contexts() throws IOException {
        Changeset liquigraphChangeSet = new Changeset();
        liquigraphChangeSet.setId("id");
        liquigraphChangeSet.setAuthor("author");
        liquigraphChangeSet.setQueries(singletonList("RETURN 42"));
        liquigraphChangeSet.setContexts("foo, bar");
        when(parser.parse(loader, "some-changelog.xml")).thenReturn(singletonList(liquigraphChangeSet));
        List<ChangeSet> expectedChangeSets = singletonList(changeSet("result.xml", "id", "author", false, true, "foo,bar", "RETURN 42"));

        migrator.migrateDeclaredChangeSets("some-changelog.xml", Collections.emptyList(), folder.newFile("result.xml"), loader);

        verify(fileWriter).write(argThat(matchesChangeSets(expectedChangeSets)), any());
    }

    @Test
    public void migrates_change_set_filtered_by_execution_contexts() throws IOException {
        Changeset liquigraphChangeSet1 = new Changeset();
        liquigraphChangeSet1.setId("id");
        liquigraphChangeSet1.setAuthor("author");
        liquigraphChangeSet1.setQueries(singletonList("RETURN 42"));
        liquigraphChangeSet1.setContexts("foo, bar");
        Changeset liquigraphChangeSet2 = new Changeset();
        liquigraphChangeSet2.setId("id2");
        liquigraphChangeSet2.setAuthor("author2");
        liquigraphChangeSet1.setQueries(singletonList("RETURN 42"));
        liquigraphChangeSet2.setContexts("bar, baz");
        when(parser.parse(loader, "some-changelog.xml")).thenReturn(asList(liquigraphChangeSet1, liquigraphChangeSet2));
        List<ChangeSet> expectedChangeSets = singletonList(changeSet("result.xml", "id", "author", false, true, "foo,bar", "RETURN 42"));

        migrator.migrateDeclaredChangeSets("some-changelog.xml", singletonList("foo"), folder.newFile("result.xml"), loader);

        verify(fileWriter).write(argThat(matchesChangeSets(expectedChangeSets)), any());
    }

    @Test
    public void migrates_change_set_with_preconditions() throws IOException {
        Changeset liquigraphChangeSet = new Changeset();
        liquigraphChangeSet.setId("id");
        liquigraphChangeSet.setAuthor("author");
        liquigraphChangeSet.setQueries(singletonList("RETURN 42"));
        liquigraphChangeSet.setContexts("foo, bar");
        liquigraphChangeSet.setPrecondition(precondition(
            MARK_AS_EXECUTED,
            and(
                or(
                    query("RETURN 2"),
                    query("RETURN 4")
                ),
                query("RETURN 6")
            )
        ));
        ChangeSet liquibaseChangeSet = changeSet("result.xml", "id", "author", false, true, "foo,bar", "RETURN 42");
        liquibaseChangeSet.setPreconditions(precondition(
            ErrorOption.MARK_RAN,
            FailOption.MARK_RAN,
            and(
                or(
                    sql("RETURN 2"),
                    sql("RETURN 4")),
                sql("RETURN 6")
            )));
        when(parser.parse(loader, "some-changelog.xml")).thenReturn(singletonList(liquigraphChangeSet));

        migrator.migrateDeclaredChangeSets("some-changelog.xml", Collections.emptyList(), folder.newFile("result.xml"), loader);

        verify(fileWriter).write(argThat(matchesChangeSets(singletonList(liquibaseChangeSet))), any());
    }

    @Test
    public void fails_to_migrate_if_change_set_defines_postconditions() {
        Changeset liquigraphChangeSet = new Changeset();
        liquigraphChangeSet.setId("id");
        liquigraphChangeSet.setAuthor("author");
        liquigraphChangeSet.setPostcondition(postcondition(query("RETURN 42")));
        when(parser.parse(loader, "some-changelog.xml")).thenReturn(singletonList(liquigraphChangeSet));

        assertThatThrownBy(() -> migrator.migrateDeclaredChangeSets("some-changelog.xml", Collections.emptyList(), folder.newFile("result.xml"), loader))
            .isInstanceOf(MigrationException.class)
            .hasMessageContaining("The following change sets define post-conditions: id by author.\n" +
                "This is not supported by Liquibase.\n" +
                "Aborting migration now.");
    }

    private ChangeSet changeSet(String path, String id, String author, boolean alwaysRun, boolean runOnChange, String contexts, String... queries) {
        ChangeSet changeSet = new ChangeSet(id, author, alwaysRun, runOnChange, path, contexts, null, null);
        stream(queries).map(RawSQLChange::new).forEach(changeSet::addChange);
        return changeSet;
    }

    private static Precondition precondition(PreconditionErrorPolicy policy, Query query) {
        Precondition precondition = new Precondition();
        precondition.setPolicy(policy);
        precondition.setQuery(query);
        return precondition;
    }

    private static PreconditionContainer precondition(ErrorOption errorPolicy,
                                                      FailOption failPolicy,
                                                      liquibase.precondition.Precondition... preconditions) {
        PreconditionContainer result = new PreconditionContainer();
        result.setOnError(errorPolicy);
        result.setOnFail(failPolicy);
        stream(preconditions).forEach(result::addNestedPrecondition);
        return result;
    }

    private static Postcondition postcondition(Query query) {
        Postcondition postcondition = new Postcondition();
        postcondition.setQuery(query);
        return postcondition;
    }

    private static Query and(Query query1, Query query2) {
        AndQuery result = new AndQuery();
        result.setQueries(asList(query1, query2));
        return result;
    }

    private static AndPrecondition and(liquibase.precondition.Precondition query1, liquibase.precondition.Precondition query2) {
        AndPrecondition result = new AndPrecondition();
        result.addNestedPrecondition(query1);
        result.addNestedPrecondition(query2);
        return result;
    }

    private static Query or(Query query1, Query query2) {
        OrQuery result = new OrQuery();
        result.setQueries(asList(query1, query2));
        return result;
    }

    private static OrPrecondition or(liquibase.precondition.Precondition query1, liquibase.precondition.Precondition query2) {
        OrPrecondition result = new OrPrecondition();
        result.addNestedPrecondition(query1);
        result.addNestedPrecondition(query2);
        return result;
    }

    private static Query query(String query) {
        SimpleQuery result = new SimpleQuery();
        result.setQuery(query);
        return result;
    }

    private static SqlPrecondition sql(String query) {
        SqlPrecondition result = new SqlPrecondition();
        result.setSql(query);
        return result;
    }
}
