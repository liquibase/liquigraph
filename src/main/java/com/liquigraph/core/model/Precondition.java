package com.liquigraph.core.model;


import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementRefs;
import java.util.Objects;

public class Precondition {

    private PreconditionErrorPolicy policy;
    private PreconditionQuery query;

    @XmlAttribute(name = "if-not-met", required = true)
    public PreconditionErrorPolicy getPolicy() {
        return policy;
    }

    public void setPolicy(PreconditionErrorPolicy policy) {
        this.policy = policy;
    }

    @XmlElementRefs({
        @XmlElementRef(name = "and", type = AndQuery.class),
        @XmlElementRef(name = "or", type = OrQuery.class),
        @XmlElementRef(name = "query", type = SimpleQuery.class)
    })
    public PreconditionQuery getQuery() {
        return query;
    }

    public void setQuery(PreconditionQuery query) {
        this.query = query;
    }

    @Override
    public int hashCode() {
        return Objects.hash(policy, query);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final Precondition other = (Precondition) obj;
        return Objects.equals(this.policy, other.policy) && Objects.equals(this.query, other.query);
    }

    @Override
    public String toString() {
        return "Precondition{" +
                "policy=" + policy +
                ", query='" + query + '\'' +
                '}';
    }
}
