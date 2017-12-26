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

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import javax.xml.transform.dom.DOMSource;
import java.util.Iterator;

class SchemaDetector {

    public boolean hasExplicitSchema(DOMSource changelog) {
        Node node = changelog.getNode();
        if (node.getNodeType() == Node.DOCUMENT_NODE) {
            return hasExplicitSchema(rootElement(node));
        }
        return hasExplicitSchema(node);
    }

    private Node rootElement(Node document) {
        Iterator<Node> iterator = new NodeListIterator(document.getChildNodes());
        while (iterator.hasNext()) {
            Node node = iterator.next();
            if (node.getNodeType() == Node.ELEMENT_NODE
                    && node.getNodeName().equals("changelog")) {
                return node;
            }
        }
        return null;
    }

    private boolean hasExplicitSchema(Node node) {
        if (node == null) {
            return false;
        }
        if (node.getNodeType() != Node.ELEMENT_NODE) {
            return false;
        }
        
        NamedNodeMap attributes = node.getAttributes();
        if (attributes == null) {
            return false;
        }
        return findAttributeByName(attributes, "xsi:noNamespaceSchemaLocation");
    }

    private boolean findAttributeByName(NamedNodeMap attributes, String name) {
        int i = 0;
        while (i < attributes.getLength()) {
            if (name.equals(attributes.item(i).getNodeName())) {
                return true;
            }
            i++;
        }
        return false;
    }
}
