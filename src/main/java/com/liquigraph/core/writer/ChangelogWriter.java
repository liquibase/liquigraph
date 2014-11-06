package com.liquigraph.core.writer;

import com.liquigraph.core.model.Changeset;

import java.util.Collection;

public interface ChangelogWriter {

    void write(Collection<Changeset> changelogsToInsert);

}
