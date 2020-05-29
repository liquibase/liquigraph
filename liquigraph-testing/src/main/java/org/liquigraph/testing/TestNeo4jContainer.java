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
package org.liquigraph.testing;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import org.testcontainers.containers.Neo4jContainer;
import org.testcontainers.utility.MountableFile;

public class TestNeo4jContainer implements AutoCloseable {

    private final Neo4jContainer<?> container;

    private TestNeo4jContainer(boolean enterprise, String password) {
        container = createContainer(imageCoordinates(enterprise), password);
    }

    public static TestNeo4jContainer createCommunityNeo4jContainer(String adminPassword) {
        return new TestNeo4jContainer(false, adminPassword);
    }

    public static TestNeo4jContainer createEnterpriseNeo4jContainer(String adminPassword) {
        return new TestNeo4jContainer(true, adminPassword);
    }

    public String getBoltUrl() {
        return container.getBoltUrl();
    }

    public String getHttpUrl() {
        return container.getHttpUrl();
    }

    public String getHttpsUrl() {
        return container.getHttpsUrl();
    }

    public void start() {
        container.start();
    }

    public void stop() {
        container.stop();
    }

    @Override
    public void close() {
        container.close();
    }

    private static Neo4jContainer<?> createContainer(String coordinates, String adminPassword) {
        String extensionJarPath = readSingleLine("/extensions.dir");
        if (!Files.isRegularFile(Paths.get(extensionJarPath))) {
            throw new RuntimeException(
                String.format("Could get not start container, no extension JAR found at path %s.%nMake sure to build the testing module first.", extensionJarPath)
            );
        }
        return new Neo4jContainer<>(coordinates)
            .withAdminPassword(adminPassword)
            .withEnv("NEO4J_ACCEPT_LICENSE_AGREEMENT", "yes")
            .withPlugins(MountableFile.forHostPath(extensionJarPath));
    }

    private static String imageCoordinates(boolean enterprise) {
        return String.format("neo4j:%s%s", projectNeo4jVersion(), enterprise ? "-enterprise" : "");
    }

    private static String projectNeo4jVersion() {
        return readSingleLine("/neo4j.version");
    }

    private static String readSingleLine(String filteredClasspathResource) {
        List<String> lines = readLines(filteredClasspathResource);
        int lineCount = lines.size();
        if (lineCount != 1) {
            throw new RuntimeException(String
            .format("%s (filtered) resource should contain exactly 1 line, found: %d", filteredClasspathResource, lineCount));
        }
        return lines.iterator().next();
    }

    private static List<String> readLines(String classpathResource) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(TestNeo4jContainer.class
        .getResourceAsStream(classpathResource)))) {

            return reader.lines().collect(Collectors.toList());
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
