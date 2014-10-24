package com.liquigraph.core.configuration;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class ConfigurationBuilderTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void fails_on_null_changelog_location() {
        thrown.expect(RuntimeException.class);
        thrown.expectMessage("'masterChangelog' should not be null");

        new ConfigurationBuilder()
            .withUri("http://localhost:7474/db/data")
            .build();
    }

    @Test
    public void fails_on_non_existing_changelog() {
        thrown.expect(RuntimeException.class);
        thrown.expectMessage("'masterChangelog' points to a non-existing location: /non-existing.xml");

        new ConfigurationBuilder()
            .withUri("http://localhost:7474/db/data")
            .withMasterChangelogLocation("/non-existing.xml")
            .build();
    }

    @Test
    public void fails_on_null_uri() {
        thrown.expect(RuntimeException.class);
        thrown.expectMessage("'uri' should not be null");

        new ConfigurationBuilder()
            .withMasterChangelogLocation("/changelog.xml")
            .build();
    }

    @Test
    public void fails_on_unsupported_protocol() {
        thrown.expect(RuntimeException.class);
        thrown.expectMessage("'uri' supports only 'file', 'http' and 'https' protocols. Given: ssh://sorry@buddy");

        new ConfigurationBuilder()
            .withMasterChangelogLocation("/changelog.xml")
            .withUri("ssh://sorry@buddy")
            .build();
    }

    @Test
    public void fails_on_malformed_uri() {
        thrown.expect(RuntimeException.class);
        thrown.expectMessage("'uri' is malformed. Given: file:///im@èè ##/so/sorry");

        new ConfigurationBuilder()
            .withMasterChangelogLocation("/changelog.xml")
            .withUri("file:///im@èè ##/so/sorry")
            .build();
    }

    @Test
    public void add_ups_all_misconfiguration_errors() {
        thrown.expect(RuntimeException.class);
        thrown.expectMessage("'masterChangelog' should not be null");
        thrown.expectMessage("'uri' should not be null");

        new ConfigurationBuilder().build();
    }
}