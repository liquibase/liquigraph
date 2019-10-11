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

import org.liquigraph.core.model.Changeset;
import org.liquigraph.core.model.Query;
import org.liquigraph.core.model.SimpleQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.lang.String.format;
import static java.nio.file.Files.createFile;
import static java.nio.file.Files.deleteIfExists;
import static java.nio.file.StandardOpenOption.APPEND;
import static java.util.stream.Collectors.joining;
import static org.liquigraph.core.exception.Throwables.propagate;

public class ChangelogFileWriter implements ChangelogWriter {

    private static final Logger LOGGER = LoggerFactory.getLogger(ChangelogFileWriter.class);

    private final ConditionPrinter conditionPrinter;
    private final File outputFile;

    public ChangelogFileWriter(ConditionPrinter conditionPrinter, File outputFile) {
        this.conditionPrinter = conditionPrinter;
        this.outputFile = outputFile;
    }

    @Override
    public void write(Collection<Changeset> changelogsToInsert) {
        try {
            LOGGER.info("About to dry-run Liquigraph. Results in file {}", outputFile.getAbsolutePath());
            Path path = outputFile.toPath();
            reinitializeFile(path);
            if (changelogsToInsert.isEmpty()) {
                writeNothingToPersist(path);
                return;
            }
            for (Changeset changeset : changelogsToInsert) {
                writeChangeset(changeset, path);
            }
        } catch (IOException e) {
            throw propagate(e);
        }
    }

    private void reinitializeFile(Path path) throws IOException {
        deleteIfExists(path);
        createFile(path);
    }

    private void writeNothingToPersist(Path path) throws IOException {
        Files.write(path, Collections.singletonList("//Liquigraph: nothing to persist!"), StandardCharsets.UTF_8);
    }

    private void writeChangeset(Changeset changeset, Path path) throws IOException {
        Files.write(path, conditionPrinter.print(changeset.getPrecondition()), StandardCharsets.UTF_8, APPEND);
        Files.write(path, changesetToString(changeset), StandardCharsets.UTF_8, APPEND);
        Files.write(path, conditionPrinter.print(changeset.getPostcondition()), StandardCharsets.UTF_8, APPEND);
    }

    private Collection<String> changesetToString(Changeset changeset) {
        Collection<String> lines = new ArrayList<>();
        lines.add(format("//Liquigraph changeset[author: %s, id: %s]", changeset.getAuthor(), changeset.getId()));
        lines.add(format("//Liquigraph changeset[executionContexts: %s]", flatten(changeset.getExecutionsContexts())));
        lines.addAll(changeset.getQueries().stream().map(ChangelogFileWriter::queryToString).collect(Collectors.toList()));
        return lines;
    }

    private static String queryToString(Query query) {
        List<String> parameters = query.getParameters();
        List<String> parameterDetails = IntStream.range(0, parameters.size())
            .mapToObj(i -> String.format("%d: %s", i + 1, parameters.get(i)))
            .collect(Collectors.toList());
        String wrappedParameters = parameters.isEmpty() ? "" : String.format(" // {%s}", String.join(", ", parameterDetails));
        return String.format("%s%s", query.getQuery(), wrappedParameters);
    }

    private String flatten(Collection<String> executionsContexts) {
        if (executionsContexts.isEmpty()) {
            return "none declared";
        }
        return String.join(",", executionsContexts);
    }

    public static void main(String[] args) {
        System.out.println(queryToString(new SimpleQuery("MATCH (n) RETURN n")));
    }
}
