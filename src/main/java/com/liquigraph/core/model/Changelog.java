package com.liquigraph.core.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Collection;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Lists.newArrayList;

@XmlAccessorType(XmlAccessType.PROPERTY)
@XmlRootElement(name = "changelog")
public class Changelog {

    private Collection<Changeset> changesets = newArrayList();

    @XmlElement(name = "changeset")
    public Collection<Changeset> getChangesets() {
        return changesets;
    }

    public void setChangesets(Collection<Changeset> changesets) {
        this.changesets = checkNotNull(changesets);
    }
}
