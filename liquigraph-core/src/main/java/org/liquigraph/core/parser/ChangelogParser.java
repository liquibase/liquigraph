package org.liquigraph.core.parser;

import org.liquigraph.core.model.Changelog;
import org.liquigraph.core.model.Changeset;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import java.util.Collection;

import static java.lang.String.format;

public final class ChangelogParser {

    private final ChangelogPreprocessor preprocessor;

    public ChangelogParser() {
        this.preprocessor = new ChangelogPreprocessor(new ImportResolver());
    }

    public Collection<Changeset> parse(ClassLoader classLoader, String masterChangelog) {
        return parseChangelog(classLoader, masterChangelog).getChangesets();
    }

    private Changelog parseChangelog(ClassLoader classLoader, String masterChangelog) {
        try {
            return (Changelog) JAXBContext.newInstance(Changelog.class)
                .createUnmarshaller()
                .unmarshal(preprocessor.preProcess(masterChangelog, classLoader));
        } catch (JAXBException e) {
            throw new IllegalArgumentException(format("Unable to parse changelog <%s>.", masterChangelog), e);
        }
    }

}
