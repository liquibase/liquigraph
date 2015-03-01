package org.liquigraph.core.parser;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.Iterator;
import java.util.NoSuchElementException;

final class NodeListIterator implements Iterator<Node> {

    private final NodeList list;
    private int i;

    NodeListIterator(NodeList list) {
        this.list = list;
    }

    @Override
    public boolean hasNext() {
        return i < list.getLength();
    }

    @Override
    public Node next() {
        Node node = list.item(i++);
        if (node == null) {
            throw new NoSuchElementException();
        }
        return node;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }
}
