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

import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSResourceResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
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

    private static class InputSourceToLSInput implements LSInput {

        private InputSource source;

        private InputSourceToLSInput(InputSource source) {
            this.source = source;
        }

        @Override
        public Reader getCharacterStream() {
            return source.getCharacterStream();
        }

        @Override
        public void setCharacterStream(Reader characterStream) {
            source.setCharacterStream(characterStream);
        }

        @Override
        public InputStream getByteStream() {
            return source.getByteStream();
        }

        @Override
        public void setByteStream(InputStream byteStream) {
            source.setByteStream(byteStream);
        }

        @Override
        public String getStringData() {
            return null;
        }

        @Override
        public void setStringData(String stringData) {
        }

        @Override
        public String getSystemId() {
            return source.getSystemId();
        }

        @Override
        public void setSystemId(String systemId) {
            source.setSystemId(systemId);
        }

        @Override
        public String getPublicId() {
            return source.getPublicId();
        }

        @Override
        public void setPublicId(String publicId) {
            source.setPublicId(publicId);
        }

        @Override
        public String getBaseURI() {
            return null;
        }

        @Override
        public void setBaseURI(String baseURI) {

        }

        @Override
        public String getEncoding() {
            return source.getEncoding();
        }

        @Override
        public void setEncoding(String encoding) {
            source.setEncoding(encoding);
        }

        @Override
        public boolean getCertifiedText() {
            return false;
        }

        @Override
        public void setCertifiedText(boolean certifiedText) {
        }

    }

    private Validator validator(SchemaErrorHandler customErrorHandler) {
        Validator validator = schema.newValidator();
        ChainedEntityResolver resolverChain = new ChainedEntityResolver();
        resolverChain.addEntityResolver(new LiquigraphLocalEntityResolver());
        resolverChain.addEntityResolver(new RedirectAwareEntityResolver());
        LSResourceResolver currentResolver = validator.getResourceResolver();
        validator.setResourceResolver((type, namespaceURI, publicId, systemId, baseURI) -> {
            try {
                InputSource input = resolverChain.resolveEntity(publicId, systemId);
                if (input != null) {
                    return new InputSourceToLSInput(input);
                }
                return currentResolver.resolveResource(type, namespaceURI, publicId, systemId, baseURI);
            } catch (SAXException | IOException e) {
                throw new RuntimeException("Could not resolve with entity resolver", e);
            }
        });
        validator.setErrorHandler(customErrorHandler);
        return validator;
    }

}
