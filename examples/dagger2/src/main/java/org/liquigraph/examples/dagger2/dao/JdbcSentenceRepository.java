/*
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
package org.liquigraph.examples.dagger2.dao;

import org.liquigraph.examples.dagger2.entity.Sentence;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;

public class JdbcSentenceRepository implements SentenceRepository {

    private final DataSource dataSource;

    public JdbcSentenceRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Optional<Sentence> findOne(String cypherQuery) {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            if (!statement.execute(cypherQuery)) {
                throw new RuntimeException("Could not execute query");
            }
            return this.extract("result", statement.getResultSet());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private Optional<Sentence> extract(String columnLabel, ResultSet results) throws SQLException {
        try (ResultSet resultSet = results) {
            resultSet.next();
            return Optional.of(new Sentence(resultSet.getString(columnLabel)));
        }
    }

}
