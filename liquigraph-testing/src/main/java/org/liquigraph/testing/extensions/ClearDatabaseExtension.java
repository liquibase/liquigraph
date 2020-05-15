/*
 * Copyright 2014-2020 the original author or authors.
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
package org.liquigraph.testing.extensions;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.schema.ConstraintDefinition;
import org.neo4j.graphdb.schema.IndexDefinition;
import org.neo4j.procedure.Context;
import org.neo4j.procedure.Mode;
import org.neo4j.procedure.Procedure;

public class ClearDatabaseExtension {

    @Context
    public GraphDatabaseService graphDb;

    @Procedure(name = "clearDb", mode = Mode.SCHEMA)
    public void clear() {
        try (Transaction transaction = graphDb.beginTx()) {
            transaction.getAllRelationships().forEach(Relationship::delete);
            transaction.getAllNodes().forEach(Node::delete);
            transaction.commit();
        }

        try (Transaction transaction = graphDb.beginTx()) {
            transaction.schema().getConstraints().forEach(ConstraintDefinition::drop);
            transaction.schema().getIndexes().forEach(IndexDefinition::drop);
            transaction.commit();
        }
    }
}
