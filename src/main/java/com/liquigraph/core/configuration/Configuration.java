package com.liquigraph.core.configuration;

import com.google.common.base.Optional;

public final class Configuration {

    private final String masterChangelog;
    private final String uri;
    private final Optional<String> username;
    private final Optional<String> password;

    Configuration(String masterChangelog, String uri, Optional<String> username, Optional<String> password) {
        this.masterChangelog = masterChangelog;
        this.uri = uri;
        this.username = username;
        this.password = password;
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
}
