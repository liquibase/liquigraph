package com.liquigraph.core.model;


import com.google.common.base.Joiner;
import com.google.common.base.Splitter;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import java.util.Collection;
import java.util.Objects;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Strings.nullToEmpty;
import static com.google.common.collect.Lists.newArrayList;
import static com.liquigraph.core.model.Checksums.checksum;

public class Changeset {

    private String id;
    private String author;
    private String query;
    private String checksum;
    private Collection<String> executionsContexts = newArrayList();
    private boolean runOnChange;
    private boolean runAlways;
    private Precondition precondition;

    @XmlAttribute(name = "id", required = true)
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @XmlAttribute(name = "author", required = true)
    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    @XmlElement(name = "query", required = true)
    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
        setChecksum(checksum(query));
    }

    @XmlTransient
    public String getChecksum() {
        return checksum;
    }

    public void setChecksum(String checksum) {
        checkArgument(checksum != null);
        checkArgument(checksum.equals(checksum(query)));
        this.checksum = checksum;
    }

    @XmlTransient
    public Collection<String> getExecutionsContexts() {
        return executionsContexts;
    }

    @XmlAttribute(name = "contexts", required = false)
    String getContexts() {
        return executionsContexts == null ? "" : Joiner.on(',').join(executionsContexts);
    }

    public void setContexts(String executionsContexts) {
        this.executionsContexts = Splitter.on(',')
            .omitEmptyStrings()
            .trimResults()
            .splitToList(nullToEmpty(executionsContexts));
    }

    @XmlAttribute(name = "run-on-change", required = false)
    public boolean isRunOnChange() {
        return runOnChange;
    }

    public void setRunOnChange(boolean runOnChange) {
        this.runOnChange = runOnChange;
    }

    @XmlAttribute(name = "run-always", required = false)
    public boolean isRunAlways() {
        return runAlways;
    }

    public void setRunAlways(boolean runAlways) {
        this.runAlways = runAlways;
    }

    @XmlElement(name = "precondition", required = false)
    public Precondition getPrecondition() {
        return precondition;
    }

    public void setPrecondition(Precondition precondition) {
        this.precondition = precondition;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, checksum);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final Changeset other = (Changeset) obj;
        return Objects.equals(this.id, other.id) && Objects.equals(this.checksum, other.checksum);
    }

    @Override
    public String toString() {
        return "Changeset{" +
                "id='" + id + '\'' +
                ", author='" + author + '\'' +
                ", query='" + query + '\'' +
                ", checksum='" + checksum + '\'' +
                ", executionsContexts=" + executionsContexts +
                ", runOnChange=" + runOnChange +
                ", runAlways=" + runAlways +
                ", precondition=" + precondition +
                '}';
    }
}
