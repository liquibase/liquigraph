package org.liquigraph.core.model.predicates;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import org.liquigraph.core.model.Changeset;

import java.util.Collection;

import static com.google.common.collect.FluentIterable.from;
import static org.liquigraph.core.model.predicates.ChangesetById.BY_ID;

public class ChangesetChecksumHasChanged implements Predicate<Changeset> {

    private final Collection<Changeset> persistedChangesets;

    private ChangesetChecksumHasChanged(Collection<Changeset> persistedChangesets) {
        this.persistedChangesets = persistedChangesets;
    }

    public static final Predicate<Changeset> CHECKSUM_HAS_CHANGED(Collection<Changeset> persistedChangesets) {
        return new ChangesetChecksumHasChanged(persistedChangesets);
    }

    @Override
    public boolean apply(Changeset input) {
        Optional<Changeset> persistedChangeset = from(persistedChangesets).firstMatch(BY_ID(input.getId()));
        return persistedChangeset.isPresent() && input.getChecksum().equals(persistedChangeset.get().getChecksum());
    }
}
