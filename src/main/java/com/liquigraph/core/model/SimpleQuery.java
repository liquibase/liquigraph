package com.liquigraph.core.model;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlValue;
import java.util.Objects;

import static java.lang.String.format;

@XmlSeeAlso(PreconditionQuery.class)
@XmlRootElement(name = "query")
public class SimpleQuery implements PreconditionQuery {

    private String query;

    @XmlValue
    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    @Override
    public int hashCode() {
        return Objects.hash(query);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final SimpleQuery other = (SimpleQuery) obj;
        return Objects.equals(this.query, other.query);
    }

    @Override
    public String toString() {
        return format("<%s>", query);
    }
}
