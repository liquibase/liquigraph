/**
 * Copyright 2014-2016 the original author or authors.
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

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;
import java.io.InputStream;

import static com.google.common.base.Throwables.propagate;

public class ImportResolver {

    private final XPath xpath;

    public ImportResolver() {
        xpath = XPathFactory.newInstance().newXPath();
    }

    public Node resolveImports(String changelog, ClassLoader classLoader) {
        try (InputStream stream = classLoader.getResourceAsStream(changelog)) {
            return resolve(changelog, document(stream), classLoader);
        } catch (IOException e) {
            throw propagate(e);
        }
    }

    private Node resolve(String changelog, Document document, ClassLoader classLoader) {
        Element root = document.getDocumentElement();

        IterableNodeList imports = IterableNodeList.of(evaluateNodes("/changelog/import", root));
        for (Node toImport: imports) {
            String fullPath = parentFolder(changelog) + attributeTextContent(toImport, "resource");
            Node resolvedRoot = resolveImports(fullPath, classLoader);
            IterableNodeList changesets = IterableNodeList.of(evaluateNodes("/changelog/changeset", resolvedRoot));
            for (Node changeset : changesets) {
                Node importedChangeset = document.importNode(changeset, true);
                root.insertBefore(importedChangeset, toImport);
            }
            root.removeChild(toImport);
        }
        return root;
    }

    private String parentFolder(String changelog) {
        if (!changelog.contains("/")) {
            return "";
        }
        return changelog.substring(0, changelog.lastIndexOf('/') + 1);
    }

    private Document document(InputStream changelog) {
        try {
            return DocumentBuilderFactory.newInstance()
                    .newDocumentBuilder()
                    .parse(changelog);
        } catch (SAXException | IOException | ParserConfigurationException e) {
            throw propagate(e);
        }
    }

    private static String attributeTextContent(Node legalImport, String attributeName) {
        return legalImport.getAttributes().getNamedItem(attributeName).getTextContent();
    }

    private NodeList evaluateNodes(String expression, Node document) {
        try {
            return (NodeList) xpath.evaluate(expression, document.getOwnerDocument(), XPathConstants.NODESET);
        } catch (XPathExpressionException e) {
            throw propagate(e);
        }
    }

}
