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
package org.liquigraph.core.io;

import org.liquigraph.core.model.CompoundQuery;
import org.liquigraph.core.model.Condition;
import org.liquigraph.core.model.Precondition;
import org.liquigraph.core.model.Query;
import org.liquigraph.core.model.SimpleQuery;

import java.util.Collection;

import static com.google.common.collect.Lists.newArrayList;
import static java.lang.String.format;

public class ConditionPrinter {

    public Collection<String> print(Precondition precondition) {
        if (precondition == null) {
            return newArrayList();
        }
        Collection<String> lines = newArrayList();
        lines.add(format("//Liquigraph precondition[if-not-met: %s]", precondition.getPolicy()));
        lines.addAll(print((Condition) precondition));
        return lines;
    }

    public Collection<String> print(Condition precondition) {
        if (precondition == null) {
            return newArrayList();
        }
        return newArrayList(traverseQuery(precondition.getQuery()));
    }

    private String traverseQuery(Query query) {
        if (query instanceof SimpleQuery) {
            return ((SimpleQuery) query).getQuery();
        }
        if (query instanceof CompoundQuery) {
            CompoundQuery compoundQuery = (CompoundQuery) query;
            return compoundQuery.compose(
                traverseQuery(compoundQuery.getFirstQuery()),
                traverseQuery(compoundQuery.getSecondQuery())
            );
        }
        throw new IllegalArgumentException(format("Unsupported query type <%s>", query.getClass().getName()));
    }
}
