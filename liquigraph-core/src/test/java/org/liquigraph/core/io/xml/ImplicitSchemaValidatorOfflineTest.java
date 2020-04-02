/*
 * Copyright 2014-2020 the original author or authors.
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.liquigraph.core.io.xml.DomSources.domStringSource;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Collection;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.dom.DOMSource;

import org.junit.Rule;
import org.junit.Test;
import org.w3c.dom.Node;

import io.sniffy.socket.DisableSockets;
import io.sniffy.test.junit.SniffyRule;

public class ImplicitSchemaValidatorOfflineTest {

    @Rule
    public SniffyRule sniffyRule = new SniffyRule();

    @Test
    @DisableSockets
    public void validates_document_with_no_internet_connection() throws Exception {
        String document =
                "<changelog>\n" +
                "    <changeset id=\"hello-world\" author=\"you\">\n" +
                "        <query>CREATE (n:Sentence {text:'Hello monde!'}) RETURN n</query>\n" +
                "    </changeset>" +
                "</changelog>";
        ImplicitSchemaValidator validator = new ImplicitSchemaValidator();

        Collection<String> errors = validator.validate(domStringSource(document));

        assertThat(errors).isEmpty();
    }
}
