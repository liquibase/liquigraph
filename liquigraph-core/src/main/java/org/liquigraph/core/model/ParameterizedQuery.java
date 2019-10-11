/*
 * Copyright 2014-2018 the original author or authors.
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

import org.liquigraph.core.exception.Preconditions;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import java.util.List;
import java.util.Objects;

import static org.liquigraph.core.exception.Preconditions.checkArgument;

@XmlRootElement(name = "parameterized-query")
public class ParameterizedQuery implements Query {

    private String query;

    private List<String> parameters;

    public ParameterizedQuery() {
    }

    // visible for testing
    public ParameterizedQuery(String query, List<String> parameters) {
        this.query = query;
        this.parameters = parameters;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    @XmlElement(name = "query")
    @Override
    public String getQuery() {
        return query;
    }

    public void setParameters(List<String> parameters) {
        this.parameters = parameters;
    }

    @XmlElement(name = "parameter")
    @Override
    public List<String> getParameters() {
        return parameters;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ParameterizedQuery that = (ParameterizedQuery) o;
        return Objects.equals(query, that.query) &&
        Objects.equals(parameters, that.parameters);
    }

    @Override
    public int hashCode() {
        return Objects.hash(query, parameters);
    }

    @Override
    public String toString() {
        return "ParameterizedQuery{" +
        "query='" + query + '\'' +
        ", parameters=" + parameters +
        '}';
    }
}
