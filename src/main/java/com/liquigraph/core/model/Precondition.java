package com.liquigraph.core.model;


import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import java.util.Objects;

@XmlAccessorType(XmlAccessType.PROPERTY)
public class Precondition {

    private PreconditionErrorPolicy policy;
    private String query;

    @XmlAttribute(name = "if-not-met", required = true)
    public PreconditionErrorPolicy getPolicy() {
        return policy;
    }

    public void setPolicy(PreconditionErrorPolicy policy) {
        this.policy = policy;
    }

    @XmlElement(name = "query", required = true)
    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
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
