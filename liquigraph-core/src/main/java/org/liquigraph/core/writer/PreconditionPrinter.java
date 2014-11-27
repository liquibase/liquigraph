package org.liquigraph.core.writer;

import org.liquigraph.core.model.CompoundQuery;
import org.liquigraph.core.model.Precondition;
import org.liquigraph.core.model.PreconditionQuery;
import org.liquigraph.core.model.SimpleQuery;

import java.util.Collection;

import static com.google.common.collect.Lists.newArrayList;
import static java.lang.String.format;

public class PreconditionPrinter {

    public Collection<String> print(Precondition precondition) {
        if (precondition == null) {
            return newArrayList();
        }
        Collection<String> lines = newArrayList();
        lines.add(format("//Liquigraph precondition[if-not-met: %s]", precondition.getPolicy()));
        lines.add(traverseQuery(precondition.getQuery()));
        return lines;
    }

    private String traverseQuery(PreconditionQuery query) {
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
