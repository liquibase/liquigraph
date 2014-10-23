package com.liquigraph.core.configuration;

import com.google.common.base.Joiner;
import com.google.common.base.Optional;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;

import static com.google.common.base.Optional.absent;
import static com.google.common.base.Optional.fromNullable;
import static com.google.common.collect.Lists.newLinkedList;
import static java.lang.String.format;

public final class ConfigurationBuilder {

    private String masterChangelog;
    private String uri;
    private Optional<String> username = absent();
    private Optional<String> password = absent();

    public ConfigurationBuilder withMasterChangelogLocation(String masterChangelog) {
        this.masterChangelog = masterChangelog;
        return this;
    }

    public ConfigurationBuilder withUri(String uri) {
        this.uri = uri;
        return this;
    }

    public ConfigurationBuilder withUsername(String username) {
        this.username = fromNullable(username);
        return this;
    }

    public ConfigurationBuilder withPassword(String password) {
        this.password = fromNullable(password);
        return this;
    }

    public Configuration build() {
        Collection<String> errors = newLinkedList();
        validateMasterChangelog(errors);
        validateUri(errors);

        if (!errors.isEmpty()) {
            throw new RuntimeException(formatErrors(errors));
        }
        return new Configuration(
            masterChangelog,
            uri,
            username,
            password
        );
    }

    private void validateMasterChangelog(Collection<String> errors) {
        String parameterName = "masterChangelog";

        if (masterChangelog == null) {
            errors.add(format("'%s' should not be null", parameterName));
        }
        else {
            try (InputStream stream = this.getClass().getResourceAsStream(masterChangelog)) {
                if (stream == null) {
                    errors.add(format("'%s' points to a non-existing location: %s", parameterName, masterChangelog));
                }
            } catch (IOException e) {
                errors.add(format("'%s' read error. Cause: %s", parameterName, e.getMessage()));
            }
        }
    }

    private void validateUri(Collection<String> errors) {
        if (uri == null) {
            errors.add("'uri' should not be null");
            return;
        }
        if (!uri.startsWith("file://") && !uri.startsWith("http://") && !uri.startsWith("https://")) {
            errors.add(format("'uri' supports only 'file', 'http' and 'https' protocols. Given: %s", uri));
            return;
        }
        try {
            new URI(uri);
        } catch (URISyntaxException e) {
            errors.add(format("'uri' is malformed. Given: %s", uri));
        }
    }

    private String formatErrors(Collection<String> errors) {
        String separator = "\n\t - ";
        return format("%s%s", separator, Joiner.on(separator).join(errors));
    }
}
