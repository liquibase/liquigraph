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

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.w3c.dom.Node;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import static org.assertj.core.api.Assertions.assertThat;

public class ImportResolverTest {

    @Rule public ExpectedException thrown = ExpectedException.none();
    private ImportResolver resolver = new ImportResolver();
    private ChangelogLoader changelogLoader = ClassLoaderChangelogLoader.currentThreadContextClassLoader();

    @Test
    public void yields_same_document_when_no_imports_are_defined() throws Exception {
        Node root = resolver.resolveImports(
            "changelog/changelog.xml",
            changelogLoader
        );

        assertThat(contents(root)).isXmlEqualTo(
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<changelog>\n" +
            "    <changeset author=\"fbiville\" id=\"first-changelog\">\n" +
            "        <query><![CDATA[MATCH (n) RETURN n]]></query>\n" +
            "    </changeset>\n" +
            "    <changeset author=\"team\" id=\"second-changelog\">\n" +
            "        <query><![CDATA[MATCH (m) RETURN m]]></query>\n" +
            "    </changeset>\n" +
            "</changelog>");
    }

    @Test
    public void yields_document_when_import_in_same_directory() throws Exception {
        Node root = resolver.resolveImports(
            "changelog/changelog-same-level-import.xml",
            changelogLoader
        );

        assertThat(contents(root)).isXmlEqualTo(
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<changelog>\n" +
            "    <changeset author=\"fbiville\" id=\"first-changelog\">\n" +
            "        <query><![CDATA[MATCH (n) RETURN n]]></query>\n" +
            "    </changeset>\n" +
            "    <changeset author=\"team\" id=\"second-changelog\">\n" +
            "        <query><![CDATA[MATCH (m) RETURN m]]></query>\n" +
            "    </changeset>\n" +
            "</changelog>");
    }

    @Test
    public void resolves_1_level_of_imports() throws Exception {
        Node root = resolver.resolveImports(
            "changelog/includes/included.xml",
            changelogLoader
        );

        assertThat(contents(root)).isXmlEqualTo(
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<changelog>\n" +
            "    <changeset author=\"fbiville\" id=\"first-changelog\">\n" +
            "        <query><![CDATA[MATCH (n) RETURN n]]></query>\n" +
            "    </changeset>\n" +
            "    <changeset author=\"team\" id=\"second-changelog\">\n" +
            "        <query><![CDATA[MATCH (m) RETURN m]]></query>\n" +
            "    </changeset>\n" +
            "    <changeset author=\"company\" id=\"third-changelog\">\n" +
            "        <query><![CDATA[MATCH (l) RETURN l]]></query>\n" +
            "    </changeset>\n" +
            "</changelog>");
    }

    @Test
    public void resolves_any_level_of_imports() throws Exception {
        Node root = resolver.resolveImports(
            "changelog/includes/deeply-nested-changelog.xml",
            changelogLoader
        );

        assertThat(contents(root)).isXmlEqualTo(
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<changelog>\n" +
            "    <changeset author=\"fbiville\" id=\"first-changelog\">\n" +
            "        <query><![CDATA[MATCH (n) RETURN n]]></query>\n" +
            "    </changeset>\n" +
            "    <changeset author=\"fbiville\" id=\"second-changelog\">\n" +
            "        <query><![CDATA[MATCH (n) RETURN n]]></query>\n" +
            "    </changeset>\n" +
            "    <changeset author=\"fbiville\" id=\"third-changelog\">\n" +
            "        <query><![CDATA[MATCH (n) RETURN n]]></query>\n" +
            "    </changeset>\n" +
            "    <changeset author=\"fbiville\" id=\"fourth-changelog\">\n" +
            "        <query><![CDATA[MATCH (n) RETURN n]]></query>\n" +
            "    </changeset>\n" +
            "</changelog>");
    }

    @Test
    public void fails_on_non_existing_import() {
        thrown.expect(RuntimeException.class);
        thrown.expectMessage("Import location cannot be resolved: changelog/includes/that/is_gonna_fail.xml");

        resolver.resolveImports(
            "changelog/includes/invalid_include.xml",
            changelogLoader
        );
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
