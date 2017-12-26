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
import static org.liquigraph.core.model.Checksums.checksum;

public class Changeset {

    private String id;
    private String author;
    private Collection<String> queries = newArrayList();
    private String checksum;
    private Collection<String> executionsContexts = newArrayList();
    private boolean runOnChange;
    private boolean runAlways;
    private Precondition precondition;
    private Postcondition postcondition;

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
    public Collection<String> getQueries() {
        return queries;
    }

    public void setQueries(Collection<String> queries) {
        checkArgument(queries != null, "Queries cannot be null");
        checkArgument(queries.size() > 0, "At least one query must be defined");
        this.queries = queries;
        setChecksum(checksum(queries));
    }

    @XmlTransient
    public String getChecksum() {
        return checksum;
    }

    public void setChecksum(String checksum) {
        checkArgument(checksum != null, "Checksum cannot be null");
        this.checksum = checksum;
    }

    @XmlTransient
    public Collection<String> getExecutionsContexts() {
        return executionsContexts;
    }

    @XmlAttribute(name = "contexts", required = false)
    String getContexts() {
        return Joiner.on(',').join(executionsContexts);
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

    @XmlElement(name = "postcondition", required = false)
    public Postcondition getPostcondition() {
        return postcondition;
    }

    public void setPostcondition(Postcondition postcondition) {
        this.postcondition = postcondition;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, author, checksum);
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
        return Objects.equals(this.id, other.id) &&
                Objects.equals(this.author, other.author) &&
                Objects.equals(this.checksum, other.checksum);
    }

    @Override
    public String toString() {
        return "Changeset{" +
                "id='" + id + '\'' +
                ", author='" + author + '\'' +
                ", queries='" + queries + '\'' +
                ", checksum='" + checksum + '\'' +
                ", executionsContexts=" + executionsContexts +
                ", runOnChange=" + runOnChange +
                ", runAlways=" + runAlways +
                ", precondition=" + precondition +
                ", postcondition=" + postcondition +
                '}';
    }

}
