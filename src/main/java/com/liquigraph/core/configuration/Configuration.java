package com.liquigraph.core.configuration;

import com.google.common.base.Optional;

public final class Configuration {

    private final String masterChangelog;
    private final String uri;
    private final Optional<String> username;
    private final Optional<String> password;
    private final ExecutionContexts executionContexts;

    Configuration(String masterChangelog, String uri, Optional<String> username, Optional<String> password) {
        this(
            masterChangelog,
            uri,
            username,
            password,
            ExecutionContexts.DEFAULT_CONTEXT
        );
    }

    Configuration(String masterChangelog,
                  String uri,
                  Optional<String> username,
                  Optional<String> password,
                  ExecutionContexts executionContexts) {

        this.masterChangelog = masterChangelog;
        this.uri = uri;
        this.username = username;
        this.password = password;
        this.executionContexts = executionContexts;
    }


    public String masterChangelog() {
        return masterChangelog;
    }

    public String uri() {
        return uri;
    }

    public Optional<String> username() {
        return username;
    }

    public Optional<String> password() {
        return password;
    }

    public ExecutionContexts executionContexts() {
        return executionContexts;
    }
}
