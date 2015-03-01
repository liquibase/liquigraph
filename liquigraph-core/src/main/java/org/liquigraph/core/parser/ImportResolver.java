package org.liquigraph.core.parser;

import com.google.common.base.Joiner;
import com.google.common.collect.FluentIterable;
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
import java.util.Collection;

import static com.google.common.base.Predicates.*;
import static com.google.common.base.Throwables.propagate;
import static org.liquigraph.core.parser.IllegalNodeToErrorMessageFunction.TO_ERROR_MESSAGE;
import static org.liquigraph.core.parser.ImportNodeRootChildPredicate.ROOT_CHILD;
import static org.liquigraph.core.parser.MissingAttributeNodePredicate.MISSING_ATTRIBUTE;

class ImportResolver {

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

        validate(evaluateNodes("//import", root));
        IterableNodeList imports = IterableNodeList.of(evaluateNodes("/changelog/import", root));
        for (Node legalImport: imports) {
            String fullPath = parentFolder(changelog) + attributeTextContent(legalImport, "resource");
            Node resolvedRoot = resolveImports(fullPath, classLoader);
            IterableNodeList changesets = IterableNodeList.of(evaluateNodes("/changelog/changeset", resolvedRoot));
            for (Node changeset : changesets) {
                Node importedChangeset = document.importNode(changeset, true);
                root.insertBefore(importedChangeset, legalImport);
            }
            root.removeChild(legalImport);
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

    private void validate(NodeList allImports) {
        Collection<String> errors = collectErrors(allImports);
        if (!errors.isEmpty()) {
            throw new IllegalArgumentException(Joiner.on("\n").join(errors));
        }
    }

    private Collection<String> collectErrors(NodeList allImports) {
        return FluentIterable.from(IterableNodeList.of(allImports))
                .filter(notNull())
                .filter(or(not(ROOT_CHILD), MISSING_ATTRIBUTE))
                .transform(TO_ERROR_MESSAGE)
                .toList();
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
