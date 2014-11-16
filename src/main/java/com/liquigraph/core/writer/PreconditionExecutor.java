package com.liquigraph.core.writer;

import com.google.common.base.Optional;
import com.liquigraph.core.exception.PreconditionException;
import com.liquigraph.core.model.CompoundQuery;
import com.liquigraph.core.model.Precondition;
import com.liquigraph.core.model.PreconditionQuery;
import com.liquigraph.core.model.SimpleQuery;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static com.google.common.base.Optional.absent;
import static com.google.common.base.Preconditions.checkArgument;
import static java.lang.String.format;

public class PreconditionExecutor {

    public final Optional<PreconditionResult> executePrecondition(Connection connection, Precondition precondition) {
        if (precondition == null) {
            return absent();
        }
        checkArgument(connection != null, "Connection should not be null");
        PreconditionResult result = new PreconditionResult(
            precondition.getPolicy(),
            applyPrecondition(connection, precondition.getQuery())
        );
        return Optional.of(result);
    }

    private boolean applyPrecondition(Connection connection, PreconditionQuery query) {
        if (query instanceof SimpleQuery) {
            SimpleQuery simpleQuery = (SimpleQuery) query;
            return execute(connection, simpleQuery.getQuery());
        }
        if (query instanceof CompoundQuery) {
            CompoundQuery compoundQuery = (CompoundQuery) query;
            return compoundQuery.compose(
                applyPrecondition(connection, compoundQuery.getFirstQuery()),
                applyPrecondition(connection, compoundQuery.getSecondQuery())
            );
        }
        throw new IllegalArgumentException(format("Unsupported query type <%s>", query.getClass().getName()));
    }

    private boolean execute(Connection connection, String query) {
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {

            resultSet.next();
            return resultSet.getBoolean("result");
        }
        catch (SQLException e) {
            throw new PreconditionException("%n\tQuery <%s> should yield exactly one column named or aliased 'result'.%n\tCause: %s", query, e.getMessage());
        }
    }
}
