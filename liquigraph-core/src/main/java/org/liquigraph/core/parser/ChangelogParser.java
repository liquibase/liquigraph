package org.liquigraph.core.parser;

import org.liquigraph.core.model.Changelog;
import org.liquigraph.core.model.Changeset;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

import static java.lang.String.format;

public final class ChangelogParser {

    public Collection<Changeset> parse(ClassLoader classLoader, String masterChangelog) {
        return parseChangelog(classLoader, masterChangelog).getChangesets();
    }

    private Changelog parseChangelog(ClassLoader classLoader, String masterChangelog) {
        try (InputStream changelogStream = classLoader.getResourceAsStream(masterChangelog)) {
            return (Changelog) JAXBContext.newInstance(Changelog.class).createUnmarshaller().unmarshal(changelogStream);
        } catch (JAXBException | IOException e) {
            throw new IllegalArgumentException(format("Unable to parse changelog <%s>.", masterChangelog), e);
        }
    }

}
