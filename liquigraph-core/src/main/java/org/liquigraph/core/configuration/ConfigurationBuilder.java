/*
 * Copyright 2014-2020 the original author or authors.
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
package org.liquigraph.core.configuration;


import org.liquigraph.core.configuration.validators.DatasourceConfigurationValidator;
import org.liquigraph.core.configuration.validators.ExecutionModeValidator;
import org.liquigraph.core.configuration.validators.MandatoryOptionValidator;
import org.liquigraph.core.configuration.validators.UserCredentialsOptionValidator;
import org.liquigraph.core.io.xml.ChangelogLoader;
import org.liquigraph.core.io.xml.ClassLoaderChangelogLoader;

import javax.sql.DataSource;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;

/**
 * Fluent {@link Configuration} builder.
 * It also validates configuration parameters.
 */
public final class ConfigurationBuilder {

    private String masterChangelog;
    private Optional<DataSource> dataSource = Optional.empty();
    private Optional<String> uri = Optional.empty();
    private Optional<String> database = Optional.empty();
    private Optional<String> username = Optional.empty();
    private Optional<String> password = Optional.empty();
    private ExecutionContexts executionContexts = ExecutionContexts.DEFAULT_CONTEXT;
    private ExecutionMode executionMode;

    private MandatoryOptionValidator mandatoryOptionValidator = new MandatoryOptionValidator();
    private DatasourceConfigurationValidator datasourceConnectionValidator = new DatasourceConfigurationValidator();
    private ExecutionModeValidator executionModeValidator = new ExecutionModeValidator();
    private UserCredentialsOptionValidator userCredentialsOptionValidator = new UserCredentialsOptionValidator();
    private ChangelogLoader changelogLoader = ClassLoaderChangelogLoader.currentThreadContextClassLoader();

    /**
     * Specifies the location of the master changelog file.
     * Please note that this location should point to a readable Liquigraph changelog file.
     *
     * @param masterChangelog Liquigraph changelog
     * @return itself for chaining purposes
     */
    public ConfigurationBuilder withMasterChangelogLocation(String masterChangelog) {
        this.masterChangelog = masterChangelog;
        return this;
    }

    /**
     * Specifies the JDBC connection URI of the graph database instance.
     * Alternatively, you can set a {@code DataSource} directly
     *
     * @param uri connection URI
     * @return itself for chaining purposes
     */
    public ConfigurationBuilder withUri(String uri) {
        this.uri = Optional.ofNullable(uri);
        return this;
    }

    /**
     * Specifies the data source of the graph database instance.
     * Alternatively, you can set the URI
     *
     * @param dataSource data source
     * @return itself for chaining purposes
     */
    public ConfigurationBuilder withDataSource(DataSource dataSource) {
        this.dataSource = Optional.ofNullable(dataSource);
        return this;
    }

    /**
     * Specifies the database to run changes on.
     *
     * @param database database
     * @return itself for chaining purposes
     */
    public ConfigurationBuilder withDatabase(String database) {
        this.database = Optional.ofNullable(database);
        return this;
    }

    /**
     * Specifies the username allowed to connect to the remote graph database instance.
     * Please be sure to provide a password, if you provide a username, too.
     *
     * @param username username
     * @return itself for chaining purposes
     */
    public ConfigurationBuilder withUsername(String username) {
        this.username = Optional.ofNullable(username);
        return this;
    }

    /**
     * Specifies the password allowed to connect to the remote graph database instance.
     * Please be sure to provide a username, if you provide a password, too.
     *
     * @param password password
     * @return itself for chaining purposes
     */
    public ConfigurationBuilder withPassword(String password) {
        this.password = Optional.ofNullable(password);
        return this;
    }

    /**
     * @param executionContexts 0 or more Liquigraph execution contexts to allow changeset
     *                          filtering
     * @return itself for chaining purposes
     * @see ConfigurationBuilder#withExecutionContexts(java.util.Collection)
     */
    public ConfigurationBuilder withExecutionContexts(String... executionContexts) {
        return withExecutionContexts(Arrays.asList(executionContexts));
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
     * Sets Liquigraph to execute changesets against the configured graph database.
     *
     * @return itself for chaining purposes
     */
    public ConfigurationBuilder withRunMode() {
        this.executionMode = RunMode.RUN_MODE;
        return this;
    }

    /**
     * Sets Liquigraph to write changesets in a <code>output.cypher</code>
     * in the specified outputDirectory.
     * Note that it won't write to the graph database.
     *
     * @param outputDirectory writable directory where the file is written
     * @return itself for chaining purposes
     */
    public ConfigurationBuilder withDryRunMode(Path outputDirectory) {
        this.executionMode = new DryRunMode(outputDirectory);
        return this;
    }

    /**
     * @param classLoader class loader
     * @return itself for chaining purposes
     * @deprecated Please use {@link #withChangelogLoader(ChangelogLoader)} with a {@link ClassLoaderChangelogLoader}.
     * <p>
     * Sets ClassLoader to use when reading Liquigraph changelogs.
     * Default is <code>Thread.currentThread().getContextClassLoader()</code>.
     * Don't call this unless you REALLY know what you're doing.
     */
    @Deprecated
    public ConfigurationBuilder withClassLoader(ClassLoader classLoader) {
        if (classLoader != null) {
            this.changelogLoader = new ClassLoaderChangelogLoader(classLoader);
        }
        return this;
    }

    /**
     * Sets {@link ChangelogLoader} to use when reading Liquigraph changelogs.
     * Default is {@link ClassLoaderChangelogLoader#currentThreadContextClassLoader()}.
     * Don't call this unless you REALLY know what you're doing.
     *
     * @param changelogLoader the changelog loader to use
     * @return itself for chaining purposes
     */
    public ConfigurationBuilder withChangelogLoader(ChangelogLoader changelogLoader) {
        if (changelogLoader != null) {
            this.changelogLoader = changelogLoader;
        }
        return this;
    }

    /**
     * Builds a {@link Configuration} instance after validating the specified
     * parameters.
     *
     * @return Liquigraph configuration
     */
    public Configuration build() {
        Collection<String> errors = new ArrayList<>();
        errors.addAll(mandatoryOptionValidator.validate(changelogLoader, masterChangelog));
        errors.addAll(datasourceConnectionValidator.validate(uri, dataSource));
        errors.addAll(executionModeValidator.validate(executionMode));
        if (database.isPresent()) {
            errors.addAll(userCredentialsOptionValidator.validateWithDatabase(username, password));
        }
        else {
            errors.addAll(userCredentialsOptionValidator.validate(username.orElse(null), password.orElse(null)));
        }

        if (!errors.isEmpty()) {
            throw new RuntimeException(formatErrors(errors));
        }

        return new Configuration(
            changelogLoader,
            masterChangelog,
            dataSourceConfiguration(),
            executionContexts,
            executionMode
        );
    }

    private ConnectionConfiguration dataSourceConfiguration() {
        if (uri.isPresent()) {
            return new ConnectionConfigurationByUri(uri.get(), database, username, password);
        }
        return new ConnectionConfigurationByDataSource(dataSource.get(), username, password);
    }

    private String formatErrors(Collection<String> errors) {
        String separator = "\n\t - ";
        return String.format("%s%s", separator, String.join(separator, errors));
    }
}
