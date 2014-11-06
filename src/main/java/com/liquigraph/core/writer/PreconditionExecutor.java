package com.liquigraph.core.writer;

import com.google.common.base.Optional;
import com.liquigraph.core.exception.PreconditionSyntaxException;
import com.liquigraph.core.model.CompoundQuery;
import com.liquigraph.core.model.Precondition;
import com.liquigraph.core.model.PreconditionQuery;
import com.liquigraph.core.model.SimpleQuery;
import org.neo4j.cypher.EntityNotFoundException;
import org.neo4j.cypher.SyntaxException;
import org.neo4j.cypher.javacompat.ExecutionEngine;
import org.neo4j.graphdb.ResourceIterator;

import static com.google.common.base.Optional.absent;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.Iterators.getOnlyElement;
import static java.lang.String.format;

public class PreconditionExecutor {

    public final Optional<PreconditionResult> executePrecondition(ExecutionEngine cypherEngine, Precondition precondition) {
        if (precondition == null) {
            return absent();
        }
        checkArgument(cypherEngine != null, "ExecutionEngine should not be null");
        PreconditionResult result = new PreconditionResult(
            precondition.getPolicy(),
            applyPrecondition(cypherEngine, precondition.getQuery())
        );
        return Optional.of(result);
    }

    private boolean applyPrecondition(ExecutionEngine cypherEngine, PreconditionQuery query) {
        if (query instanceof SimpleQuery) {
            SimpleQuery simpleQuery = (SimpleQuery) query;
            return execute(cypherEngine, simpleQuery.getQuery());
        }
        if (query instanceof CompoundQuery) {
            CompoundQuery compoundQuery = (CompoundQuery) query;
            return compoundQuery.compose(
                applyPrecondition(cypherEngine, compoundQuery.getFirstQuery()),
                applyPrecondition(cypherEngine, compoundQuery.getSecondQuery())
            );
        }
        throw new IllegalArgumentException(format("Unsupported query type <%s>", query.getClass().getName()));
    }

    private boolean execute(ExecutionEngine cypherEngine, String query) {
        try (ResourceIterator<Boolean> results = cypherEngine.execute(query).columnAs("result")) {
            return getOnlyElement(results);
        }
        catch (EntityNotFoundException e) {
            throw new PreconditionSyntaxException("%n\tQuery <%s> should yield exactly one column named or aliased 'result'.", query);
        }
        catch (SyntaxException e) {
            throw new PreconditionSyntaxException(
                e.getCause(),
                "%n\tQuery <%s> is invalid. Please check again its syntax.%n\tMore details:\n%s",
                query,
                e.getMessage()
            );
        }
    }
}
