package com.liquigraph.core.api;

import com.liquigraph.core.configuration.ExecutionContexts;
import com.liquigraph.core.model.Changeset;
import org.junit.Test;

import java.util.Collection;

import static com.google.common.collect.Lists.newArrayList;
import static com.liquigraph.core.configuration.ExecutionContexts.DEFAULT_CONTEXT;
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