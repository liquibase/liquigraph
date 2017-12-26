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

import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

class ImplicitSchemaValidator implements DomSourceValidator {

    private final Schema schema;

    public ImplicitSchemaValidator() {
        schema = implicitSchema("/schema/changelog.xsd");
    }

    @Override
    public Collection<String> validate(DOMSource source) throws Exception {
        SchemaErrorHandler errorHandler = new SchemaErrorHandler();
        validator(errorHandler).validate(source);
        return errorHandler.getErrors();
    }

    private static Schema implicitSchema(String name) {
        try (InputStream stream = XmlSchemaValidator.class.getResourceAsStream(name)) {
            return SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI).newSchema(new StreamSource(stream));
        } catch (SAXException | IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private Validator validator(SchemaErrorHandler customErrorHandler) {
        Validator validator = schema.newValidator();
        validator.setErrorHandler(customErrorHandler);
        return validator;
    }

}
