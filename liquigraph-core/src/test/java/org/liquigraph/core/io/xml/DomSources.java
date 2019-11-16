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

import java.io.ByteArrayInputStream;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.dom.DOMSource;

import org.w3c.dom.Node;

public class DomSources {

    public static DOMSource domStringSource(String xml) throws Exception {
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
