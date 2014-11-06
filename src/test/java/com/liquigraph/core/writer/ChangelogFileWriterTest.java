package com.liquigraph.core.writer;

import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.liquigraph.core.model.Changeset;
import com.liquigraph.core.model.Precondition;
import org.jboss.byteman.contrib.bmunit.BMRule;
import org.jboss.byteman.contrib.bmunit.BMUnitRunner;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collection;
import java.util.Collections;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.isA;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(BMUnitRunner.class)
public class ChangelogFileWriterTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    private ChangelogWriter writer;
    private File outputFile;

    @Before
    public void prepare() throws IOException {
        PreconditionPrinter preconditionPrinter = given_precondition_printer_prints_nothing();
        outputFile = new File(temporaryFolder.newFolder(), "output.cypher");
        writer = new ChangelogFileWriter(
            preconditionPrinter,
            outputFile
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

    @BMRule(
        name = "write failed",
        targetClass = "java.nio.file.Files",
        targetMethod = "write(Path,Iterable,Charset,OpenOption[])",
        action = "throw new IOException(\"bazinga\")"
    )
    @Test
    public void propagates_exception() {
        thrown.expect(RuntimeException.class);
        thrown.expectCause(isA(IOException.class));
        thrown.expectMessage("bazinga");

        writer.write(newArrayList(
            changeset("identifier", "fbiville", "CREATE (n: SomeNode {text:'yeah'})")
        ));
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
        changeset.setQuery(query);
        return changeset;
    }

    private PreconditionPrinter given_precondition_printer_prints_nothing() {
        PreconditionPrinter preconditionPrinter = mock(PreconditionPrinter.class);
        when(preconditionPrinter.print(any(Precondition.class))).thenReturn(Collections.<String>emptyList());
        return preconditionPrinter;
    }

}
