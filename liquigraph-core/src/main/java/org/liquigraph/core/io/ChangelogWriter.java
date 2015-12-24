package org.liquigraph.core.io;

import org.liquigraph.core.model.Changeset;

import java.util.Collection;

public interface ChangelogWriter {

    void write(Collection<Changeset> changelogsToInsert);

}
