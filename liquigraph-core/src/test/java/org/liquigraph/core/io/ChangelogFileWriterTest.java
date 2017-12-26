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
import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;
import org.liquigraph.core.model.Changeset;
import org.liquigraph.core.model.Precondition;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collection;
import java.util.Collections;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ChangelogFileWriterTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    private ChangelogWriter writer;
    private File outputFile;

    @Before
    public void prepare() throws IOException {
        ConditionPrinter conditionPrinter = given_precondition_printer_prints_nothing();
        outputFile = new File(temporaryFolder.newFolder(), "output.cypher");
        writer = new ChangelogFileWriter(
                conditionPrinter,
            outputFile
        );
    }

    @Test
    public void generates_a_simple_comment_when_nothing_has_to_be_written() throws IOException {
        writer.write(Lists.<Changeset>newArrayList());

        String fileContents = Joiner.on("\n").join(Files.readAllLines(outputFile.toPath(), Charsets.UTF_8));
        assertThat(fileContents).isEqualTo(
            "//Liquigraph: nothing to persist!"
        );
    }

    @Test
    public void persists_one_changeset_on_file() throws IOException {
        Collection<Changeset> changesets = newArrayList(
            changeset("identifier", "fbiville", "CREATE (n: SomeNode {text:'yeah'})")
        );

        writer.write(changesets);

        String fileContents = Joiner.on("\n").join(Files.readAllLines(outputFile.toPath(), Charsets.UTF_8));
        assertThat(fileContents).isEqualTo(
            "//Liquigraph changeset[author: fbiville, id: identifier]\n" +
            "//Liquigraph changeset[executionContexts: none declared]\n" +
            "CREATE (n: SomeNode {text:'yeah'})"
        );
    }

    @Test
    public void persists_several_changesets_on_file() throws IOException {
        Collection<Changeset> changesets = newArrayList(
            changeset("identifier", "fbiville", "CREATE (n: SomeNode {text:'yeah'})"),
            changeset("identifier2", "mgazanayi", "CREATE (n2: SomeNode {text:'yeah'})", "preprod,prod")
        );

        writer.write(changesets);

        String fileContents = Joiner.on("\n").join(Files.readAllLines(outputFile.toPath(), Charsets.UTF_8));
        assertThat(fileContents).isEqualTo(
            "//Liquigraph changeset[author: fbiville, id: identifier]\n" +
            "//Liquigraph changeset[executionContexts: none declared]\n" +
            "CREATE (n: SomeNode {text:'yeah'})\n" +
            "//Liquigraph changeset[author: mgazanayi, id: identifier2]\n" +
            "//Liquigraph changeset[executionContexts: preprod,prod]\n" +
            "CREATE (n2: SomeNode {text:'yeah'})"
        );
    }

    private Changeset changeset(String identifier, String author, String query, String executionContexts) {
        Changeset changeset = changeset(identifier, author, query);
        changeset.setContexts(executionContexts);
        return changeset;
    }

    private Changeset changeset(String identifier, String author, String query) {
        Changeset changeset = new Changeset();
        changeset.setId(identifier);
        changeset.setAuthor(author);
        changeset.setQueries(singletonList(query));
        return changeset;
    }

    private ConditionPrinter given_precondition_printer_prints_nothing() {
        ConditionPrinter conditionPrinter = mock(ConditionPrinter.class);
        when(conditionPrinter.print(any(Precondition.class))).thenReturn(Collections.<String>emptyList());
        return conditionPrinter;
    }

}
