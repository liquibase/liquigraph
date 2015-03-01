package org.liquigraph.core.parser;

import org.junit.Test;
import org.w3c.dom.Element;

import static org.assertj.core.api.Assertions.assertThat;
import static org.liquigraph.core.parser.DocumentElements.documentElement;
import static org.liquigraph.core.parser.ImportNodeRootChildPredicate.ROOT_CHILD;
import static org.liquigraph.core.parser.NodeFinder.findNode;

public class ImportNodeRootChildPredicateTest {


    @Test
    public void keeps_node_if_direct_child_to_root_element() throws Exception {
        Element documentRoot = documentElement(
                "<changelog>\n" +
                "    <import resource=\"hello-world.xml\" />\n" +
                "</changelog>");

        assertThat(ROOT_CHILD.apply(findNode(documentRoot, "(//import)[1]"))).isTrue();
    }

    @Test
    public void does_not_keep_misplaced_imports() throws Exception {
        Element documentRoot = documentElement(
                "<changelog>\n" +
                "   <changeset>\n" +
                "       <import resource=\"hello-world.xml\" />\n" +
                "   </changeset>\n" +
                "</changelog>");

        assertThat(ROOT_CHILD.apply(findNode(documentRoot, "(//import)[1]"))).isFalse();
    }

    @Test
    public void does_not_keep_hacky_misplaced_imports() throws Exception {
        Element documentRoot = documentElement(
                "<changelog>\n" +
                "   <changelog>\n" +
                "       <import resource=\"hello-world.xml\" />\n" +
                "   </changelog>\n" +
                "</changelog>");

        assertThat(ROOT_CHILD.apply(findNode(documentRoot, "(//import)[1]"))).isFalse();
    }


}