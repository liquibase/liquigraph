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

import org.liquigraph.core.exception.ConditionExecutionException;
import org.liquigraph.core.model.CompoundConditionQuery;
import org.liquigraph.core.model.Condition;
import org.liquigraph.core.model.ConditionQuery;
import org.liquigraph.core.model.SimpleConditionQuery;
import org.neo4j.driver.v1.exceptions.ClientException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static java.lang.String.format;
import static org.liquigraph.core.exception.Preconditions.checkArgument;

public class ConditionExecutor {

    private final Logger logger = LoggerFactory.getLogger(ConditionExecutor.class);

    public final boolean executeCondition(Connection connection, Condition condition) {
        checkArgument(connection != null, "Connection should not be null");
        return applyPrecondition(connection, condition.getQuery());
    }

    private boolean applyPrecondition(Connection connection, ConditionQuery query) {
        if (query instanceof SimpleConditionQuery) {
            SimpleConditionQuery simpleQuery = (SimpleConditionQuery) query;
            return execute(connection, simpleQuery.getQuery());
        }
        if (query instanceof CompoundConditionQuery) {
            CompoundConditionQuery compoundQuery = (CompoundConditionQuery) query;
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
            logger.debug("Executed condition: {}", query);
            resultSet.next();
            return resultSet.getBoolean("result");
        }
        catch (SQLException | ClientException e) {
            throw new ConditionExecutionException("%nError executing condition:%n" +
               "\tMake sure your query <%s> yields exactly one column named or aliased 'result'.%n" +
               "\tActual cause: %s", query, e.getMessage());
        }
    }
}
