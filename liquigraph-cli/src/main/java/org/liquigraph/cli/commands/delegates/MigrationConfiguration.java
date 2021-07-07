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
package org.liquigraph.cli.commands.delegates;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

import com.beust.jcommander.Parameter;
import org.liquigraph.cli.io.ClassLoaders;
import org.liquigraph.cli.io.Files;
import org.liquigraph.core.io.xml.ChangelogLoader;
import org.liquigraph.core.io.xml.ClassLoaderChangelogLoader;

import static java.util.Collections.emptyList;

public class MigrationConfiguration {

    @Parameter(
        names = {"--changelog", "-c"},
        description = "Master Liquigraph changelog location.\n" +
            "\t Prefix with 'classpath:' if location is in classpath",
        required = true
    )
    private String changelog;

    @Parameter(
        names = {"--execution-contexts", "-x"},
        description = "Comma-separated list of Liquigraph execution contexts"
    )
    private String executionContexts = "";

    public String getChangelog() {
        return fileName(changelog);
    }

    public Collection<String> getExecutionContexts() {
        return toCollection(executionContexts);
    }

    public ChangelogLoader getChangelogLoader() {
        return new ClassLoaderChangelogLoader(ClassLoaders.urlClassLoader(getResourceUrl()));
    }

    public URL getResourceUrl() {
        return Files.toUrl(parentFolder(changelog));
    }

    private static String fileName(String changelog) {
        return new File(changelog).getName();
    }

    private static Collection<String> toCollection(String executionContexts) {
        if (executionContexts.isEmpty()) {
            return emptyList();
        }
        Collection<String> result = new ArrayList<>();
        for (String context : executionContexts.split(",")) {
            result.add(context.trim());
        }
        return result;
    }

    private static String parentFolder(String changelog) {
        try {
            return new File(changelog).getCanonicalFile().getParent();
        }
        catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MigrationConfiguration that = (MigrationConfiguration) o;
        return Objects.equals(changelog, that.changelog) &&
            Objects.equals(executionContexts, that.executionContexts);
    }

    @Override
    public int hashCode() {
        return Objects.hash(changelog, executionContexts);
    }
}
