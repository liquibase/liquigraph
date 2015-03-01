package org.liquigraph.core.parser;

import org.junit.Test;
import org.w3c.dom.Node;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import static java.lang.Thread.currentThread;
import static org.assertj.core.api.Assertions.assertThat;

public class ImportResolverTest {

    private ImportResolver resolver = new ImportResolver();
    private ClassLoader classLoader = currentThread().getContextClassLoader();

    @Test
    public void yields_same_document_when_no_imports_are_defined() throws Exception {
        Node root = resolver.resolveImports(
            "changelog.xml",
            classLoader
        );

        assertThat(contents(root)).isXmlEqualTo(
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<changelog>\n" +
            "    <changeset author=\"fbiville\" id=\"first-changelog\">\n" +
            "        <query><![CDATA[MATCH n RETURN n]]></query>\n" +
            "    </changeset>\n" +
            "    <changeset author=\"team\" id=\"second-changelog\">\n" +
            "        <query><![CDATA[MATCH m RETURN m]]></query>\n" +
            "    </changeset>\n" +
            "</changelog>");
    }

    @Test
    public void resolves_1_level_of_imports() throws Exception {
        Node root = resolver.resolveImports(
            "includes/included.xml",
            classLoader
        );

        assertThat(contents(root)).isXmlEqualTo(
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<changelog>\n" +
            "    <changeset author=\"fbiville\" id=\"first-changelog\">\n" +
            "        <query><![CDATA[MATCH n RETURN n]]></query>\n" +
            "    </changeset>\n" +
            "    <changeset author=\"team\" id=\"second-changelog\">\n" +
            "        <query><![CDATA[MATCH m RETURN m]]></query>\n" +
            "    </changeset>\n" +
            "    <changeset author=\"company\" id=\"third-changelog\">\n" +
            "        <query><![CDATA[MATCH l RETURN l]]></query>\n" +
            "    </changeset>\n" +
            "</changelog>");
    }

    @Test
    public void resolves_any_level_of_imports() throws Exception {
        Node root = resolver.resolveImports(
            "includes/deeply-nested-changelog.xml",
            classLoader
        );

        assertThat(contents(root)).isXmlEqualTo(
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<changelog>\n" +
            "    <changeset author=\"fbiville\" id=\"first-changelog\">\n" +
            "        <query><![CDATA[MATCH n RETURN n]]></query>\n" +
            "    </changeset>\n" +
            "    <changeset author=\"fbiville\" id=\"second-changelog\">\n" +
            "        <query><![CDATA[MATCH n RETURN n]]></query>\n" +
            "    </changeset>\n" +
            "    <changeset author=\"fbiville\" id=\"third-changelog\">\n" +
            "        <query><![CDATA[MATCH n RETURN n]]></query>\n" +
            "    </changeset>\n" +
            "    <changeset author=\"fbiville\" id=\"fourth-changelog\">\n" +
            "        <query><![CDATA[MATCH n RETURN n]]></query>\n" +
            "    </changeset>\n" +
            "</changelog>");
    }

    private String contents(Node root) throws Exception {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            print(root, out);
            return out.toString("UTF-8");
        }
    }

    private void print(Node root, OutputStream out) throws Exception {
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
        transformer.setOutputProperty(OutputKeys.METHOD, "xml");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

        try (OutputStreamWriter writer = new OutputStreamWriter(out, "UTF-8")) {
            transformer.transform(new DOMSource(root), new StreamResult(writer));
        }
    }
}