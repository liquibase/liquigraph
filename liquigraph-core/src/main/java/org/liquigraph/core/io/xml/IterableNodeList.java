package org.liquigraph.core.io.xml;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.Iterator;

import static com.google.common.base.Preconditions.checkState;

final class IterableNodeList implements Iterable<Node> {

    private final NodeList nodes;

    private IterableNodeList(NodeList nodeList) {
        checkState(nodeList != null);
        this.nodes = nodeList;
    }

    public static IterableNodeList of(NodeList list) {
        return new IterableNodeList(list);
    }

    @Override
    public Iterator<Node> iterator() {
        return new NodeListIterator(nodes);
    }
}
