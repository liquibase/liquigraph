/*
 * Copyright 2014-2021 the original author or authors.
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

import liquibase.ContextExpression;
import liquibase.change.CheckSum;
import liquibase.changelog.ChangeSet;
import liquibase.precondition.Precondition;
import liquibase.precondition.PreconditionLogic;
import liquibase.precondition.core.AndPrecondition;
import liquibase.precondition.core.OrPrecondition;
import liquibase.precondition.core.PreconditionContainer;
import liquibase.precondition.core.SqlPrecondition;
import org.mockito.ArgumentMatcher;

import java.util.List;
import java.util.stream.Collectors;

public class LiquibaseChangeSetsMatcher implements ArgumentMatcher<List<ChangeSet>> {

    private final List<ChangeSet> expected;

    private LiquibaseChangeSetsMatcher(List<ChangeSet> expected) {
        this.expected = expected;
    }

    public static ArgumentMatcher<List<ChangeSet>> matchesChangeSets(List<ChangeSet> expected) {
        return new LiquibaseChangeSetsMatcher(expected);
    }

    @Override
    public boolean matches(List<ChangeSet> actual) {
        int size = expected.size();
        if (size != actual.size()) {
            return false;
        }
        for (int i = 0; i < size; i++) {
            if (!equal(expected.get(i), actual.get(i))) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String toString() {
        return expected.stream()
            .map(c -> String.format(
                "{toString: \"%s\", preconditions:%s, contexts: \"%s\", checkSum: %s}",
                c,
                printPreconditions(c.getPreconditions()), c.getContexts(), c.generateCheckSum()))
            .collect(Collectors.joining(", "));
    }

    private static boolean equal(ChangeSet expected, ChangeSet actual) {
        if (!expected.equals(actual)) {
            return false;
        }
        ContextExpression expectedContexts = expected.getContexts();
        ContextExpression actualContexts = actual.getContexts();
        if (!expectedContexts.getContexts().equals(actualContexts.getContexts())) {
            return false;
        }
        if (!equalPreconditions(expected.getPreconditions(), actual.getPreconditions())) {
            return false;
        }
        CheckSum expectedCheckSum = expected.generateCheckSum();
        CheckSum actualCheckSum = actual.generateCheckSum();
        return expectedCheckSum.equals(actualCheckSum);
    }

    private static boolean equalPreconditions(PreconditionContainer expectedPrecondition, PreconditionContainer actualPrecondition) {
        if (expectedPrecondition == null ^ actualPrecondition == null) {
            return false;
        }
        if (expectedPrecondition == null) {
            return true;
        }
        if (!expectedPrecondition.getName().equals(actualPrecondition.getName())) {
            return false;
        }
        if (!expectedPrecondition.getOnError().equals(actualPrecondition.getOnError())) {
            return false;
        }
        if (!expectedPrecondition.getOnFail().equals(actualPrecondition.getOnFail())) {
            return false;
        }
        return nestedPreconditionsEqual(
            expectedPrecondition.getNestedPreconditions(),
            actualPrecondition.getNestedPreconditions()
        );
    }

    private static boolean nestedPreconditionsEqual(List<Precondition> expectedNestedPreconditions, List<Precondition> actualNestedPreconditions) {
        int size = expectedNestedPreconditions.size();
        if (size != actualNestedPreconditions.size()) {
            return false;
        }
        for (int i = 0; i < size; i++) {
            if (!equalPrecondition(expectedNestedPreconditions.get(i), actualNestedPreconditions.get(i))) {
                return false;
            }
        }
        return true;
    }

    private static boolean equalPrecondition(Precondition expected, Precondition actual) {
        if (!expected.getClass().equals(actual.getClass())) {
            return false;
        }
        if (expected instanceof AndPrecondition || expected instanceof OrPrecondition) {
            List<Precondition> expectedPreconditions = ((PreconditionLogic) expected).getNestedPreconditions();
            List<Precondition> actualPreconditions = ((PreconditionLogic) actual).getNestedPreconditions();
            return nestedPreconditionsEqual(expectedPreconditions, actualPreconditions);
        }
        if (expected instanceof SqlPrecondition) {
            SqlPrecondition expectedSql = (SqlPrecondition) expected;
            SqlPrecondition actualSql = (SqlPrecondition) actual;
            return expectedSql.getSql().equals(actualSql.getSql());
        }
        throw unsupportedPreconditionType(expected);
    }

    private static String printPreconditions(PreconditionContainer preconditions) {
        if (preconditions == null) {
            return "null";
        }
        List<Precondition> nestedPreconditions = preconditions.getNestedPreconditions();
        StringBuilder builder = new StringBuilder();
        for (Precondition precondition : nestedPreconditions) {
            builder.append(printPrecondition(precondition));
        }
        return builder.toString();
    }

    private static String printPrecondition(Precondition precondition) {
        if (precondition instanceof AndPrecondition) {
            return joinCompoundPrecondition(
                ((AndPrecondition) precondition).getNestedPreconditions(),
                " AND ");
        }
        if (precondition instanceof OrPrecondition) {
            return joinCompoundPrecondition(
                ((OrPrecondition) precondition).getNestedPreconditions(),
                " OR ");
        }
        if (precondition instanceof SqlPrecondition) {
            return ((SqlPrecondition) precondition).getSql();
        }
        throw unsupportedPreconditionType(precondition);
    }

    private static String joinCompoundPrecondition(List<Precondition> nestedPreconditions, String operator) {
        return nestedPreconditions
            .stream()
            .map(LiquibaseChangeSetsMatcher::printPrecondition)
            .collect(Collectors.joining(operator, "(", ")"));
    }

    private static IllegalArgumentException unsupportedPreconditionType(Precondition precondition) {
        return new IllegalArgumentException(String.format(
            "Unsupported precondition type %s, expected one of: %s, %s, %s",
            precondition.getClass(),
            AndPrecondition.class,
            OrPrecondition.class,
            SqlPrecondition.class
        ));
    }
}
