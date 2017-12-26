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

import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import org.liquigraph.core.model.Changeset;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;

import static com.google.common.base.Throwables.propagate;
import static com.google.common.collect.Lists.newArrayList;
import static java.lang.String.format;
import static java.nio.file.Files.createFile;
import static java.nio.file.Files.deleteIfExists;
import static java.nio.file.StandardOpenOption.APPEND;

public class ChangelogFileWriter implements ChangelogWriter {

    private final ConditionPrinter conditionPrinter;
    private final File outputFile;

    public ChangelogFileWriter(ConditionPrinter conditionPrinter, File outputFile) {
        this.conditionPrinter = conditionPrinter;
        this.outputFile = outputFile;
    }

    @Override
    public void write(Collection<Changeset> changelogsToInsert) {
        try {
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
        Files.write(path, newArrayList("//Liquigraph: nothing to persist!"), Charsets.UTF_8);
    }

    private void writeChangeset(Changeset changeset, Path path) throws IOException {
        Files.write(path, conditionPrinter.print(changeset.getPrecondition()), Charsets.UTF_8, APPEND);
        Files.write(path, changesetToString(changeset), Charsets.UTF_8, APPEND);
        Files.write(path, conditionPrinter.print(changeset.getPostcondition()), Charsets.UTF_8, APPEND);
    }

    private Collection<String> changesetToString(Changeset changeset) {
        Collection<String> lines = newArrayList();
        lines.add(format("//Liquigraph changeset[author: %s, id: %s]", changeset.getAuthor(), changeset.getId()));
        lines.add(format("//Liquigraph changeset[executionContexts: %s]", flatten(changeset.getExecutionsContexts())));
        lines.addAll(changeset.getQueries());
        return lines;
    }

    private String flatten(Collection<String> executionsContexts) {
        if (executionsContexts.isEmpty()) {
            return "none declared";
        }
        return Joiner.on(",").join(executionsContexts);
    }

}
