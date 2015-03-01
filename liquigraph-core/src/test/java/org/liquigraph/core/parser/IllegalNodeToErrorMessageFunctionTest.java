package org.liquigraph.core.parser;

import org.junit.Test;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import static org.assertj.core.api.Assertions.assertThat;
import static org.liquigraph.core.parser.DocumentElements.documentElement;
import static org.liquigraph.core.parser.IllegalNodeToErrorMessageFunction.TO_ERROR_MESSAGE;
import static org.liquigraph.core.parser.NodeFinder.findNode;

public class IllegalNodeToErrorMessageFunctionTest {

    @Test
    public void converts_illegal_import_to_error_message() throws Exception {
        Element documentRoot = documentElement(
                "<changelog>\n" +
                "   <toto>\n" +
                "       <import resource=\"hello-world.xml\" />\n" +
                "   </toto>\n" +
                "</changelog>");

        Node illegalNode = findNode(documentRoot, "(//import)[1]");

        assertThat(TO_ERROR_MESSAGE.apply(illegalNode))
            .isEqualTo(
                "\n<import> can only appear at top level of the XML document and must define exactly one resource attribute.\n" +
                "\n\tNode path: </changelog/toto/import/>" +
                "\n\tResource attribute value: <hello-world.xml>" +
                "\n");
    }
}