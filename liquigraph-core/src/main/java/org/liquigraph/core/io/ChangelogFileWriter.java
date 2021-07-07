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

import org.liquigraph.core.model.Changeset;
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

import static java.lang.String.format;
import static java.nio.file.Files.createFile;
import static java.nio.file.Files.deleteIfExists;
import static java.nio.file.StandardOpenOption.APPEND;
import static org.liquigraph.core.exception.Throwables.propagate;

public class ChangelogFileWriter implements ChangelogWriter {

    private static final Logger LOGGER = LoggerFactory.getLogger(ChangelogFileWriter.class);

    private final ConditionPrinter conditionPrinter;
    private final String database;
    private final File outputFile;

    public ChangelogFileWriter(ConditionPrinter conditionPrinter, String database, File outputFile) {
        this.conditionPrinter = conditionPrinter;
        this.database = database;
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
            writeHeaderMaybe(path);
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
        String nothingToDoMessage = String.format("//Liquigraph%s: nothing to persist!", databaseInstanceOrEmpty());
        Files.write(path, Collections.singletonList(nothingToDoMessage), StandardCharsets.UTF_8);
    }

    private void writeHeaderMaybe(Path path) throws IOException {
        if (database == null) {
            return;
        }
        String header = String.format("//Liquigraph (instance: %s)", database);
        Files.write(path, Collections.singletonList(header), StandardCharsets.UTF_8);
    }

    private String databaseInstanceOrEmpty() {
        return database == null ? "" : " (instance " + database + ")";
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
        lines.addAll(changeset.getQueries());
        return lines;
    }

    private String flatten(Collection<String> executionsContexts) {
        if (executionsContexts.isEmpty()) {
            return "none declared";
        }
        return String.join(",", executionsContexts);
    }

}
