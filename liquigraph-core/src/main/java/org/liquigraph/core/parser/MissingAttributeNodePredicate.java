package org.liquigraph.core.parser;

import com.google.common.base.Predicate;
import org.w3c.dom.Node;

enum MissingAttributeNodePredicate implements Predicate<Node> {

    MISSING_ATTRIBUTE;

    @Override
    public boolean apply(Node node) {
        return node.getAttributes().getNamedItem("resource") == null;
    }
}
