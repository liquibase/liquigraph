package org.liquigraph.core.api;

import org.liquigraph.core.configuration.ExecutionContexts;
import org.liquigraph.core.model.Changeset;
import org.junit.Test;

import java.util.Collection;

import static com.google.common.collect.Lists.newArrayList;
import static org.liquigraph.core.configuration.ExecutionContexts.DEFAULT_CONTEXT;
import static org.assertj.core.api.Assertions.assertThat;

public class ChangelogDiffMakerTest {

    private ChangelogDiffMaker diffMaker = new ChangelogDiffMaker();

    @Test
    public void diff_includes_all_latest_changesets_with_default_execution_context() {
        Collection<Changeset> changesets = diffMaker.computeChangesetsToInsert(
            DEFAULT_CONTEXT,
            newArrayList(changeset("ID", "fbiville", "CREATE n"), changeset("ID2", "fbiville", "CREATE m")),
            newArrayList(changeset("ID", "fbiville", "CREATE n"))
        );

        assertThat(changesets).containsExactly(changeset("ID2", "fbiville", "CREATE m"));
    }

    @Test
    public void diff_includes_all_latest_changesets_without_declared_context() {
        Collection<Changeset> changesets = diffMaker.computeChangesetsToInsert(
            new ExecutionContexts(newArrayList("foo")),
            newArrayList(changeset("ID", "fbiville", "CREATE n"), changeset("ID2", "fbiville", "CREATE m")),
            newArrayList(changeset("ID", "fbiville", "CREATE n"))
        );

        assertThat(changesets).containsExactly(changeset("ID2", "fbiville", "CREATE m"));
    }

    @Test
    public void diff_includes_contextless_and_context_matching_changesets() {
        Collection<Changeset> changesets = diffMaker.computeChangesetsToInsert(
            new ExecutionContexts(newArrayList("foo", "bar")),
            newArrayList(
                changeset("ID", "fbiville", "CREATE n"),
                changeset("ID2", "fbiville", "CREATE m", "bar"),
                changeset("ID3", "fbiville", "CREATE l", "foo"),
                changeset("ID4", "fbiville", "CREATE k", "foo,baz"),
                changeset("ID5", "fbiville", "CREATE j", "baz"),
                changeset("ID6", "fbiville", "CREATE i", "foo,bar"),
                changeset("ID7", "fbiville", "CREATE h")
            ),
            newArrayList(changeset("ID", "fbiville", "CREATE n"))
        );

        assertThat(changesets)
            .extracting("id")
            .containsExactly("ID2", "ID3", "ID4", "ID6", "ID7");
    }

    @Test
    public void diff_includes_run_always_changesets() {
        Collection<Changeset> changesets = diffMaker.computeChangesetsToInsert(
            DEFAULT_CONTEXT,
            newArrayList(
                changeset("ID", "fbiville", "CREATE n", "", true, false),
                changeset("ID2", "fbiville", "CREATE m", "", true, false)
            ),
            newArrayList(changeset("ID", "fbiville", "CREATE n"))
        );

        assertThat(changesets)
            .extracting("id")
            .containsExactly("ID", "ID2");
    }

    @Test
    public void diff_does_not_include_run_always_changesets_if_they_do_not_match_any_execution_context() {
        Collection<Changeset> changesets = diffMaker.computeChangesetsToInsert(
            new ExecutionContexts(newArrayList("foo", "bar")),
            newArrayList(
                changeset("ID", "fbiville", "CREATE n", "baz", true, false),
                changeset("ID2", "fbiville", "CREATE m", "foo", true, false)
            ),
            newArrayList(changeset("ID", "fbiville", "CREATE n"))
        );

        assertThat(changesets)
            .extracting("id")
            .containsExactly("ID2");
    }

    @Test
    public void diff_includes_run_on_change_changesets_that_never_ran_or_were_altered_since_last_execution() {
        Collection<Changeset> changesets = diffMaker.computeChangesetsToInsert(
            DEFAULT_CONTEXT,
            newArrayList(
                changeset("ID", "fbiville", "CREATE n2 RETURN n2", "baz", false, true),
                changeset("ID2", "fbiville", "CREATE m", "foo", false, true)
            ),
            newArrayList(changeset("ID", "fbiville", "CREATE n"))
        );

        assertThat(changesets)
            .extracting("id")
            .containsExactly("ID", "ID2");
    }

    @Test
    public void diff_does_not_include_run_on_change_changesets_if_they_do_not_match_any_execution_context() {
        Collection<Changeset> changesets = diffMaker.computeChangesetsToInsert(
            new ExecutionContexts(newArrayList("foo", "bar")),
            newArrayList(
                changeset("ID", "fbiville", "CREATE n2 RETURN n2", "bar", false, true),
                changeset("ID2", "fbiville", "CREATE m", "baz", false, true)
            ),
            newArrayList(changeset("ID", "fbiville", "CREATE n"))
        );

        assertThat(changesets)
            .extracting("id")
            .containsExactly("ID");
    }

    @Test
    public void diff_includes_changesets_that_run_always_and_on_change() {
        Collection<Changeset> changesets = diffMaker.computeChangesetsToInsert(
            DEFAULT_CONTEXT,
            newArrayList(
                changeset("ID", "fbiville", "CREATE n", null, true, true),
                changeset("ID2", "fbiville", "CREATE m2", null, true, true)
            ),
            newArrayList(
                changeset("ID", "fbiville", "CREATE n"),
                changeset("ID2", "fbiville", "CREATE m")
            )
        );

        assertThat(changesets)
            .extracting("id")
            .containsExactly("ID", "ID2");
    }

    @Test
    public void diff_does_not_include_changesets_that_run_always_and_on_change_if_they_do_not_match_any_execution_context() {
        Collection<Changeset> changesets = diffMaker.computeChangesetsToInsert(
            new ExecutionContexts(newArrayList("foo", "bar")),
            newArrayList(
                changeset("ID", "fbiville", "CREATE n", "baz", true, true),
                changeset("ID2", "fbiville", "CREATE m2", "foo", true, true)
            ),
            newArrayList(
                changeset("ID", "fbiville", "CREATE n"),
                changeset("ID2", "fbiville", "CREATE m")
            )
        );

        assertThat(changesets)
            .extracting("id")
            .containsExactly("ID2");
    }

    private Changeset changeset(String id, String author, String query, String contexts, boolean runAlways, boolean runOnChange) {
        Changeset changeset = changeset(id, author, query, contexts);
        changeset.setRunAlways(runAlways);
        changeset.setRunOnChange(runOnChange);
        return changeset;
    }

    private Changeset changeset(String id, String author, String query, String contexts) {
        Changeset changeset = changeset(id, author, query);
        changeset.setContexts(contexts);
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