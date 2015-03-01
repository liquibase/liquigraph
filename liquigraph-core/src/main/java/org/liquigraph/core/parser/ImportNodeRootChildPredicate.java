package org.liquigraph.core.parser;

import com.google.common.base.Predicate;
import org.w3c.dom.Node;

enum ImportNodeRootChildPredicate implements Predicate<Node> {

    ROOT_CHILD;

    @Override
    public boolean apply(Node input) {
        Node changelog = input.getParentNode();
        if (changelog == null || !changelog.getNodeName().equals("changelog")) {
            return false;
        }
        Node documentRoot = changelog.getParentNode();
        return documentRoot != null && documentRoot.getNodeType() == Node.DOCUMENT_NODE;
    }
}
