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
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Lists.newLinkedList;
import static java.lang.String.format;

/**
 * Fluent {@link com.liquigraph.core.configuration.Configuration} builder.
 * It also validates configuration parameters.
 */
public final class ConfigurationBuilder {

    private String masterChangelog;
    private String uri;
    private Optional<String> username = absent();
    private Optional<String> password = absent();
    private ExecutionContexts executionContexts = ExecutionContexts.DEFAULT_CONTEXT;

    /**
     * Specifies the location of the master changelog file.
     * Please note that this location should point to a readable Liquigraph changelog file.
     * @param masterChangelog Liquigraph changelog
     * @return itself for chaining purposes
     */
    public ConfigurationBuilder withMasterChangelogLocation(String masterChangelog) {
        this.masterChangelog = masterChangelog;
        return this;
    }

    /**
     * Specifies the connection URI of the graph database instance.
     * If the graph database is embedded, please use 'file://' as a prefix.
     * Otherwise, only 'http://' and 'https://' are supported at the moment.
     *
     * @param uri connection URI
     * @return itself for chaining purposes
     */
    public ConfigurationBuilder withUri(String uri) {
        this.uri = uri;
        return this;
    }

    /**
     * Specifies the username allowed to connect to the remote graph database instance.
     * @param username username
     * @return itself for chaining purposes
     */
    public ConfigurationBuilder withUsername(String username) {
        this.username = fromNullable(username);
        return this;
    }

    /**
     * Specifies the password allowed to connect to the remote graph database instance.
     * @param password password
     * @return itself for chaining purposes
     */
    public ConfigurationBuilder withPassword(String password) {
        this.password = fromNullable(password);
        return this;
    }

    /**
     * @see com.liquigraph.core.configuration.ConfigurationBuilder#withExecutionContexts(java.util.Collection)
     */
    public ConfigurationBuilder withExecutionContexts(String... executionContexts) {
        return withExecutionContexts(newArrayList(executionContexts));
    }

    /**
     * Specifies one or more execution contexts.
     * 
     * @param executionContexts non-nullable execution contexts
     * @return itself for chaining purposes
     */
    public ConfigurationBuilder withExecutionContexts(Collection<String> executionContexts) {
        if (!executionContexts.isEmpty()) {
            this.executionContexts = new ExecutionContexts(executionContexts);
        }
        return this;
    }

    /**
     * Builds a {@link com.liquigraph.core.configuration.Configuration} instance after validating the specified
     * parameters.
     *
     * @return Liquigraph configuration
     */
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
            password,
            executionContexts
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
