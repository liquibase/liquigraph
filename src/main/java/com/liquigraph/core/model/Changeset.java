package com.liquigraph.core.model;


import javax.xml.bind.annotation.*;
import java.util.Objects;

import static com.google.common.base.Preconditions.checkArgument;
import static com.liquigraph.core.model.Checksums.checksum;

@XmlAccessorType(XmlAccessType.PROPERTY)
public class Changeset {

    private String id;
    private String author;
    private String query;
    private String checksum;

    @XmlAttribute(name = "id")
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @XmlAttribute(name = "author")
    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    @XmlElement(name = "query")
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
}
