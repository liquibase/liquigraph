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
package org.liquigraph.maven;

import org.apache.maven.plugins.annotations.Parameter;

abstract class JdbcConnectionMojoBase extends ProjectAwareMojo {

    /**
     * Graph JDBC URI
     * <ul>
     *  <li>jdbc:neo4j:http://&lt;host&gt;:&lt;port&gt;/</li>
     *  <li>jdbc:neo4j:https://&lt;host&gt;:&lt;port&gt;/</li>
     *  <li>jdbc:neo4j:bolt://&lt;host&gt;:&lt;port&gt;/</li>
     *  <li>jdbc:neo4j:bolt+s://&lt;host&gt;:&lt;port&gt;/</li>
     *  <li>jdbc:neo4j:bolt+ssc://&lt;host&gt;:&lt;port&gt;/</li>
     *  <li>jdbc:neo4j:neo4j://&lt;host&gt;:&lt;port&gt;/</li>
     *  <li>jdbc:neo4j:neo4j+s://&lt;host&gt;:&lt;port&gt;/</li>
     *  <li>jdbc:neo4j:neo4j+ssc://&lt;host&gt;:&lt;port&gt;/</li>
     * </ul>
     */
    @Parameter(property = "jdbcUri", required = true)
    String jdbcUri;

    /**
     * Graph connection database. Default instance is targeted if an explicit database name is not provided.
     */
    @Parameter(property = "database")
    String database;

    /**
     * Graph connection username.
     */
    @Parameter(property = "username")
    String username;

    /**
     * Graph connection password.
     */
    @Parameter(property = "password")
    String password;
}
