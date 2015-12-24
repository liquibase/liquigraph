package org.liquigraph.core.io.xml;

import org.w3c.dom.Node;
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

import static java.lang.String.format;

public class XmlSchemaValidator {

    private final Schema schema;

    public XmlSchemaValidator() {
        try (InputStream stream = getClass().getResourceAsStream("/schema/changelog.xsd")) {
            schema = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI).newSchema(new StreamSource(stream));
        } catch (SAXException | IOException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Validate the fully resolved changelog containing all migrations
     *
     * @throws IllegalArgumentException if there is am XML validation error
     */
    public Collection<String> validateSchema(Node changelog) {
        return validate(new DOMSource(changelog));
    }

    private Collection<String> validate(DOMSource changelog) {
        try {
            SchemaErrorHandler customErrorHandler = new SchemaErrorHandler();
            validator(customErrorHandler).validate(changelog);
            return customErrorHandler.getErrors();
        } catch (IOException | SAXException e) {
            throw new IllegalArgumentException(
                format("Exception while reading changelog: %n\t%s.", e.getMessage()),
                e
            );
        }
    }

    private Validator validator(SchemaErrorHandler customErrorHandler) {
        Validator validator = schema.newValidator();
        validator.setErrorHandler(customErrorHandler);
        return validator;
    }

}
