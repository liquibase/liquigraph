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

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.XMLReader;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.Collection;

class ExplicitSchemaValidator implements DomSourceValidator {

    private final SAXParserFactory saxParserFactory;

    public ExplicitSchemaValidator() {
        saxParserFactory = SAXParserFactory.newInstance();
        saxParserFactory.setValidating(true);
        saxParserFactory.setNamespaceAware(true);
    }

    @Override
    public Collection<String> validate(DOMSource changelog) throws Exception {
        SchemaErrorHandler errorHandler = new SchemaErrorHandler();
        XMLReader reader = saxParser().getXMLReader();
        reader.setErrorHandler(errorHandler);
        parse(changelog, reader);
        return errorHandler.getErrors();
    }

    private SAXParser saxParser() throws ParserConfigurationException, SAXException {
        SAXParser saxParser = saxParserFactory.newSAXParser();
        try {
            saxParser.setProperty(
                "http://java.sun.com/xml/jaxp/properties/schemaLanguage",
                "http://www.w3.org/2001/XMLSchema"
            );
        } catch (SAXNotRecognizedException | SAXNotSupportedException ignored) {
        }
        return saxParser;
    }

    // quick'n'dirty: this is super inefficient
    private void parse(DOMSource changelog, XMLReader reader) throws IOException, TransformerException, SAXException {
        try (StringWriter writer = new StringWriter()) {
            StreamResult result = new StreamResult(writer);
            TransformerFactory.newInstance().newTransformer().transform(changelog, result);
            try (InputStream inputStream = new ByteArrayInputStream(writer.toString().getBytes("UTF-8"))) {
                reader.parse(new InputSource(inputStream));
            }
        }
    }
}
