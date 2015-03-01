package org.liquigraph.core.parser;

import com.google.common.base.Function;
import org.w3c.dom.Node;

enum IllegalNodeToErrorMessageFunction implements Function<Node, String> {

    TO_ERROR_MESSAGE;

    @Override
    public String apply(Node input) {
        String resource = resourceLocation(input.getAttributes().getNamedItem("resource"));
        String fullPath = fullName(input);
        return String.format(
            "%n<import> can only appear at top level of the XML document and must define" +
                " exactly one resource attribute.%n" +
                "%n\tNode path: </%s>" +
                "%n\tResource attribute value: <%s>%n",
            fullPath,
            resource
        );
    }

    private static String resourceLocation(Node resource) {
        if (resource != null) {
            return resource.getTextContent();
        }
        return "__missing__";
    }

    private static String fullName(Node input) {
        return fullName(input, "");
    }

    private static String fullName(Node input, String path) {
        if (input.getParentNode() == null) {
            return path;
        }
        return fullName(
            input.getParentNode(),
            String.format("%s/%s", input.getNodeName(), path)
        );
    }
}
