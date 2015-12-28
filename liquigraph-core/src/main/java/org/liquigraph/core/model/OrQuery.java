/**
 * Copyright 2014-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.liquigraph.core.model;

import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementRefs;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import java.util.List;
import java.util.Objects;

import static com.google.common.collect.Lists.newArrayList;
import static java.lang.String.format;
import static org.liquigraph.core.model.CompoundQueries.checkQueryListState;

@XmlSeeAlso(PreconditionQuery.class)
@XmlRootElement(name = "or")
public class OrQuery implements CompoundQuery {

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
    public boolean compose(boolean firstResult, boolean secondResult) {
        return firstResult || secondResult;
    }

    @Override
    public String compose(String firstQuery, String secondQuery) {
        return format("((%s) OR (%s))", firstQuery, secondQuery);
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
        final OrQuery other = (OrQuery) obj;
        return Objects.equals(this.preconditionQueries, other.preconditionQueries);
    }

    @Override
    public String toString() {
        return format("<%s> OR <%s>", getFirstQuery(), getSecondQuery());
    }
}
