/*
 * Copyright 2014-2021 the original author or authors.
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
import java.util.Objects;

public class Postcondition implements Condition {

    private Query query;

    @XmlElementRefs({
        @XmlElementRef(name = "and", type = AndQuery.class),
        @XmlElementRef(name = "or", type = OrQuery.class),
        @XmlElementRef(name = "query", type = SimpleQuery.class)
    })
    @Override
    public Query getQuery() {
        return query;
    }

    public void setQuery(Query query) {
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
        final Postcondition other = (Postcondition) obj;
        return Objects.equals(this.query, other.query);
    }

    @Override
    public String toString() {
        return "Postcondition{" +
                "query='" + query + '\'' +
                '}';
    }
}
