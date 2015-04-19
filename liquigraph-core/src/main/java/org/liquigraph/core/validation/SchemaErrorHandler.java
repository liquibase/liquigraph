package org.liquigraph.core.validation;

import org.liquigraph.core.parser.ChangelogParser;
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
    private final Collection<String> errors = new ArrayList<>();

    public Collection<String> getErrors() {
        return errors;
    }

    @Override
    public void warning(SAXParseException exception) throws SAXException {
        LOGGER.warn("XSD validation warning : {}", new Object[]{exception.getMessage()});
    }

    @Override
    public void error(SAXParseException exception) throws SAXException {
        LOGGER.error("XSD validation error : {}", new Object[]{exception.getMessage()});
        errors.add(exception.getMessage());
    }

    @Override
    public void fatalError(SAXParseException exception) throws SAXException {
        LOGGER.error("XSD validation fatal : {}", new Object[]{exception.getMessage()});
        errors.add(exception.getMessage());
    }
}
