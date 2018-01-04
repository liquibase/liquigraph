/*
 * Copyright 2014-2018 the original author or authors.
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
package org.liquigraph.spring.starter;

import org.liquigraph.spring.SpringLiquigraph;

import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.validation.constraints.NotNull;
import org.springframework.boot.context.properties.DeprecatedConfigurationProperty;

/**
 * Configuration properties to configure {@link SpringLiquigraph}.
 *
 * @author Michael Vitz
 * @author Florent Biville
 */
@ConfigurationProperties(prefix = "liquigraph", ignoreUnknownFields = false)
public final class LiquigraphProperties {

    /**
     * Change log configuration path.
     * Favor 'changelog' over 'changeLog' property name.
     */
    @NotNull
    private String changeLog = "classpath:/db/liquigraph/changelog.xml";

    /**
     * Enable liquigraph.
     */
    private boolean enabled = true;

    /**
     * Login user of the database to migrate.
     */
    private String user;

    /**
     * Login password of the database to migrate.
     */
    private String password;

    /**
     * JDBC URL of the database to migrate.
     * If not set, the primary configured data source is used.
     */
    private String url;

    /**
     * Liquigraph runtime execution contexts.
     *
     * Changesets will match:
     *  - if they define no execution context
     *  - if one of their contexts matches one of the runtime contexts
     */
    private String[] executionContexts = new String[0];

    public void setChangelog(String changelog) {
        setChangeLog(changelog);
    }

    @DeprecatedConfigurationProperty(reason = "Typo", replacement = "changelog")
    public String getChangeLog() {
        return changeLog;
    }

    public void setChangeLog(String changeLog) {
        this.changeLog = changeLog;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String[] getExecutionContexts() {
        return executionContexts;
    }

    public void setExecutionContexts(String[] executionContexts) {
        this.executionContexts = executionContexts;
    }
}
