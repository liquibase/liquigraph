package com.liquigraph.core.parser;

import com.liquigraph.core.model.Changelog;
import com.liquigraph.core.model.Changeset;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

import static java.lang.String.format;

public final class ChangelogParser {

    public Collection<Changeset> parse(String masterChangelog) {
        return parseChangelog(masterChangelog).getChangesets();
    }

    private Changelog parseChangelog(String masterChangelog) {
        try (InputStream changelogStream = this.getClass().getResourceAsStream(masterChangelog)) {
            return (Changelog) JAXBContext.newInstance(Changelog.class).createUnmarshaller().unmarshal(changelogStream);
        } catch (JAXBException | IOException e) {
            throw new IllegalArgumentException(format("Unable to parse changelog <%s>.", masterChangelog), e);
        }
    }

}
