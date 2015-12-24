package org.liquigraph.core.io.xml;

import com.google.common.collect.ImmutableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import java.util.ArrayList;
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
