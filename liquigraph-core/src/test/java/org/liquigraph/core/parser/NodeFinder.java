package org.liquigraph.core.parser;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

class NodeFinder {

    private static final XPath XPATH = XPathFactory.newInstance().newXPath();

    public static Node findNode(Element node, String expression) throws XPathExpressionException {
        return (Node) XPATH.evaluate(expression, node, XPathConstants.NODE);
    }
}
