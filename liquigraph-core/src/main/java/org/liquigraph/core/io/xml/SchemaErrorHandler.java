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

import com.google.common.collect.ImmutableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import java.util.Collection;

/**
 * Nested class that keeps validator errors as a collection
 */
class SchemaErrorHandler implements ErrorHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ChangelogParser.class);
    private final ImmutableList.Builder<String> errors = ImmutableList.builder();

    public Collection<String> getErrors() {
        return errors.build();
    }

    @Override
    public void warning(SAXParseException exception) throws SAXException {
        LOGGER.warn("XSD validation warning : {}", exception.getMessage());
    }

    @Override
    public void error(SAXParseException exception) throws SAXException {
        LOGGER.error("XSD validation error : {}", exception.getMessage());
        errors.add(exception.getMessage());
    }

    @Override
    public void fatalError(SAXParseException exception) throws SAXException {
        LOGGER.error("XSD validation fatal : {}", exception.getMessage());
        errors.add(exception.getMessage());
    }
}
