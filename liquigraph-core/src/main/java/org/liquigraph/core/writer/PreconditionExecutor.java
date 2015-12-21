package org.liquigraph.core.writer;

import com.google.common.base.Optional;
import org.liquigraph.core.exception.PreconditionException;
import org.liquigraph.core.model.CompoundQuery;
import org.liquigraph.core.model.Precondition;
import org.liquigraph.core.model.PreconditionQuery;
import org.liquigraph.core.model.SimpleQuery;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static com.google.common.base.Optional.absent;
import static com.google.common.base.Preconditions.checkArgument;
import static java.lang.String.format;

public class PreconditionExecutor {

    public final PreconditionResult executePrecondition(Connection connection, Precondition precondition) {
        checkArgument(connection != null, "Connection should not be null");
        return new PreconditionResult(
            precondition.getPolicy(),
            applyPrecondition(connection, precondition.getQuery())
        );
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
            throw new PreconditionException("%nError executing precondition:%n" +
               "\tMake sure your query <%s> yields exactly one column named or aliased 'result'.%n" +
               "\tActual cause: %s", query, e.getMessage());
        }
    }
}
