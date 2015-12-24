package org.liquigraph.core.io.xml;

import com.google.common.base.Joiner;
import org.liquigraph.core.model.Changelog;
import org.liquigraph.core.model.Changeset;
import org.w3c.dom.Node;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.util.Collection;

import static java.lang.String.format;

public final class ChangelogParser {

    private final ChangelogPreprocessor preprocessor;
    private final Unmarshaller unmarshaller;
    private final XmlSchemaValidator validator;

    public ChangelogParser(XmlSchemaValidator validator, ChangelogPreprocessor preprocessor) {
        this.validator = validator;
        this.preprocessor = preprocessor;
        try {
            unmarshaller = JAXBContext.newInstance(Changelog.class).createUnmarshaller();
        } catch (JAXBException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    /**
     * Parses a <code>masterChangelog</code> XML file from the specified classloader,
     * to a <code>Changelog</code> object.
     *
     * @param classLoader     The classloader in which we search the masterChangelog
     * @param masterChangelog Filename of the master changelog
     * @throws IllegalArgumentException if there is an error during the conversion
     * @return A <code>Changelog</code> object that correspond to the XML file
     */
    public Collection<Changeset> parse(ClassLoader classLoader, String masterChangelog) {
        return parseChangelog(classLoader, masterChangelog).getChangesets();
    }

    private Changelog parseChangelog(ClassLoader classLoader, String masterChangelog) {
        try {
            Node document = preprocessor.preProcess(masterChangelog, classLoader);
            Collection<String> errors = validator.validateSchema(document);
            if (!errors.isEmpty()) {
                throw new IllegalArgumentException(formatErrorMessage(errors));
            }
            return (Changelog) unmarshaller.unmarshal(document);
        } catch (JAXBException e) {
            throw new IllegalArgumentException(format("Unable to parse changelog <%s>.", masterChangelog), e);
        }
    }

    private String formatErrorMessage(Collection<String> errors) {
        String separator = "\n\t";
        return separator + Joiner.on(separator).join(errors);
    }
}
