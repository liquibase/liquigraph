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
