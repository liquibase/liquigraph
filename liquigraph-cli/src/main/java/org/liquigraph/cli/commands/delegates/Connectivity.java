/*
 * Copyright 2014-2022 the original author or authors.
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
package org.liquigraph.cli.commands.delegates;

import java.util.Objects;

import com.beust.jcommander.Parameter;

public class Connectivity {

    @Parameter(
        names = {"--graph-db-uri", "-g"},
        description = "Graph JDBC URI:\n" +
            "\t- jdbc:neo4j:http://host:port/\n" +
            "\t- jdbc:neo4j:https://host:port/\n" +
            "\t- jdbc:neo4j:bolt://host:port/\n",
        required = true
    )
    private String graphDbUri;

    @Parameter(
        names = {"--username", "-u"},
        description = "Graph DB username (remote only)"
    )
    private String username;

    @Parameter(
        names = {"--password", "-p"},
        description = "Graph DB password (remote only)",
        password = true
    )
    private String password;

    public String getGraphDbUri() {
        return graphDbUri;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Connectivity that = (Connectivity) o;
        return Objects.equals(graphDbUri, that.graphDbUri) &&
            Objects.equals(username, that.username) &&
            Objects.equals(password, that.password);
    }

    @Override
    public int hashCode() {
        return Objects.hash(graphDbUri, username, password);
    }
}
