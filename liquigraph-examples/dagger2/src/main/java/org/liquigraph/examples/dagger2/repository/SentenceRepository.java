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
package org.liquigraph.examples.dagger2.repository;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;
import javax.inject.Inject;
import javax.sql.DataSource;
import org.liquigraph.examples.dagger2.domain.Sentence;

public class SentenceRepository {

    private final DataSource dataSource;

    @Inject
    public SentenceRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Optional<Sentence> findOne() {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            return findSentence(statement);
        } catch (SQLException e) {
            return Optional.empty();
        }
    }

    private Optional<Sentence> findSentence(Statement statement) throws SQLException {
        try (ResultSet resultSet = statement.executeQuery("MATCH (n:Sentence) RETURN n.text AS result")) {
            if (!resultSet.next()) {
                return Optional.empty();
            }
            return Optional.of(new Sentence(resultSet.getString("result")));
        }
    }
}
