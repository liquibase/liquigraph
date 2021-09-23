/*
 * Copyright 2014-2021 the original author or authors.
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

import org.liquigraph.core.io.ChangelogLoader;
import org.liquigraph.core.io.ChangelogParser;
import org.liquigraph.core.model.Changelog;
import org.liquigraph.core.model.Changeset;
import org.w3c.dom.Node;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.util.Collection;

import static java.lang.String.format;

public final class ChangelogXmlParser implements ChangelogParser {

    private static final String SEPARATOR = System.lineSeparator() + "\t";
    private final ChangelogPreprocessor preprocessor;
    private final Unmarshaller unmarshaller;
    private final XmlSchemaValidator validator;

    public ChangelogXmlParser(XmlSchemaValidator validator, ChangelogPreprocessor preprocessor) {
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
     * @param changelogLoader The changelog loader which loads the masterChangelog
     * @param mainChangelogPath Filename of the master changelog
     * @return A <code>Changelog</code> object that correspond to the XML file
     * @throws IllegalArgumentException if there is an error during the conversion
     */
    @Override
    public Collection<Changeset> parse(ChangelogLoader changelogLoader, String mainChangelogPath) {
        return parseChangelog(changelogLoader, mainChangelogPath).getChangesets();
    }

    private Changelog parseChangelog(ChangelogLoader changelogLoader, String masterChangelog) {
        try {
            Node document = preprocessor.preProcess(masterChangelog, changelogLoader);
            Collection<String> errors = validator.validateSchema(document);
            if (!errors.isEmpty()) {
                throw new IllegalArgumentException(formatErrorMessage(errors));
            }
            Changelog changelog = (Changelog) unmarshaller.unmarshal(document);
            fixUpChangesets(changelog);
            return changelog;
        } catch (JAXBException e) {
            throw new IllegalArgumentException(format("Unable to parse changelog <%s>.", masterChangelog), e);
        }
    }

    /*
     * Ugly workaround due to the way JAXB handles setters
     * The checksum invariant of changesets cannot be properly
     * managed.
     */
    private void fixUpChangesets(Changelog changelog) {
        for (Changeset changeset : changelog.getChangesets()) {
            changeset.setQueries(changeset.getQueries());
        }
    }

    private String formatErrorMessage(Collection<String> errors) {
        return SEPARATOR + String.join(SEPARATOR, errors);
    }
}
