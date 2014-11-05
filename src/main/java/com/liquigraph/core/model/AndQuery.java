package com.liquigraph.core.model;

import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementRefs;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import java.util.List;
import java.util.Objects;

import static com.google.common.collect.Lists.newArrayList;
import static com.liquigraph.core.model.CompoundQueries.checkQueryListState;
import static java.lang.String.format;

@XmlSeeAlso(PreconditionQuery.class)
@XmlRootElement(name = "and")
public class AndQuery implements CompoundQuery {

    private List<PreconditionQuery> preconditionQueries = newArrayList();

    @XmlElementRefs({
        @XmlElementRef(name = "and", type = AndQuery.class),
        @XmlElementRef(name = "or", type = OrQuery.class),
        @XmlElementRef(name = "query", type = SimpleQuery.class)
    })
    public List<PreconditionQuery> getPreconditionQueries() {
        return preconditionQueries;
    }

    public void setPreconditionQueries(List<PreconditionQuery> preconditionQueries) {
        this.preconditionQueries = preconditionQueries;
    }

    @Override
    public PreconditionQuery getFirstQuery() {
        checkQueryListState(preconditionQueries);
        return preconditionQueries.get(0);
    }

    @Override
    public PreconditionQuery getSecondQuery() {
        checkQueryListState(preconditionQueries);
        return preconditionQueries.get(1);
    }

    @Override
    public int hashCode() {
        return Objects.hash(preconditionQueries);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final AndQuery other = (AndQuery) obj;
        return Objects.equals(this.preconditionQueries, other.preconditionQueries);
    }

    @Override
    public String toString() {
        return format("<%s> AND <%s>", getFirstQuery(), getSecondQuery());
    }
}
