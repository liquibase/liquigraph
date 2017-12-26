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

public class DomSourceValidatorFactoryTest {

    private final DomSourceValidatorFactory factory = new DomSourceValidatorFactory();

    @Test
    public void instantiates_an_implicit_schema_validator_when_no_schema_is_set() throws Exception {
        DomSourceValidator validator = factory.createValidator(domSource("<changelog></changelog>"));

        assertThat(validator).isInstanceOf(ImplicitSchemaValidator.class);
    }

    @Test
    public void instantiates_an_explicit_schema_validator_when_schema_is_set() throws Exception {
        String document =
                "<changelog xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" \n" +
                "           xsi:noNamespaceSchemaLocation=\"http://liquigraph.org/schema/1.0-RC3/liquigraph.xsd\">\n" +
                "</changelog>";
        DomSourceValidator validator = factory.createValidator(domSource(document));

        assertThat(validator).isInstanceOf(ExplicitSchemaValidator.class);
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