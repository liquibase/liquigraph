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

import org.junit.Test;
import org.w3c.dom.Node;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.dom.DOMSource;
import java.io.ByteArrayInputStream;

import static org.assertj.core.api.Assertions.assertThat;

public class SchemaDetectorTest {

    private final SchemaDetector detector = new SchemaDetector();

    @Test
    public void detects_explicit_schema_declaration() throws Exception {
        String document =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<changelog xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
            "           xsi:noNamespaceSchemaLocation=\"http://liquigraph.org/schema/1.0-RC3/liquigraph.xsd\">\n" +
            "</changelog>";

        assertThat(detector.hasExplicitSchema(domSource(document))).isTrue();
    }

    @Test
    public void detects_explicit_schema_declaration_preceded_with_comments() throws Exception {
        String document =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<!-- here is an awesome comment -->\n" +
            "<changelog xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" \n" +
            "           xsi:noNamespaceSchemaLocation=\"http://liquigraph.org/schema/1.0-RC3/liquigraph.xsd\">\n" +
            "</changelog>";

        assertThat(detector.hasExplicitSchema(domSource(document))).isTrue();
    }

    @Test
    public void detects_implicit_schema() throws Exception {
        String document =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<changelog></changelog>";

        assertThat(detector.hasExplicitSchema(domSource(document))).isFalse();
    }

    private static DOMSource domSource(String xml) throws Exception {
        return new DOMSource(root(xml));
    }

    private static Node root(String xml) throws Exception {
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(xml.getBytes())) {
            return DocumentBuilderFactory
                    .newInstance()
                    .newDocumentBuilder()
                    .parse(inputStream)
                    .getDocumentElement();
        }
    }
}