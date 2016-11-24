/*
 * Copyright 2014-2016 the original author or authors.
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

import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import org.liquigraph.core.configuration.validators.ConnectionConfigurationValidator;
import org.liquigraph.core.configuration.validators.ExecutionModeValidator;
import org.liquigraph.core.configuration.validators.MandatoryOptionValidator;
import org.liquigraph.core.configuration.validators.UserCredentialsOptionValidator;
import org.neo4j.graphdb.GraphDatabaseService;
import org.restlet.engine.Engine;

import javax.sql.DataSource;
import java.nio.file.Path;
import java.util.Collection;
import java.util.logging.Level;

import static com.google.common.base.Optional.absent;
import static com.google.common.base.Optional.fromNullable;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Lists.newLinkedList;
import static java.lang.String.format;

/**
 * Fluent {@link Configuration} builder.
 * It also validates configuration parameters.
 */
public final class ConfigurationBuilder {

    private String masterChangelog;
    private Optional<DataSource> dataSource = absent();
    private Optional<String> uri = absent();
    private Optional<GraphDatabaseService> database = absent();
    private Optional<String> username = absent();
    private Optional<String> password = absent();
    private ExecutionContexts executionContexts = ExecutionContexts.DEFAULT_CONTEXT;
    private ExecutionMode executionMode;

    private MandatoryOptionValidator mandatoryOptionValidator = new MandatoryOptionValidator();
    private ConnectionConfigurationValidator datasourceConnectionValidator = new ConnectionConfigurationValidator();
    private ExecutionModeValidator executionModeValidator = new ExecutionModeValidator();
    private UserCredentialsOptionValidator userCredentialsOptionValidator = new UserCredentialsOptionValidator();
    private ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

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
     * Specifies the JDBC connection URI of the graph database instance.
     * Alternatively, you can directly set a {@code DataSource} or a
     * {@code GraphDataseService}.
     *
     * @param uri connection URI
     * @return itself for chaining purposes
     */
    public ConfigurationBuilder withUri(String uri) {
        this.uri = fromNullable(uri);
        return this;
    }

    /**
     * Specifies the {@code DataSource} instance.
     * Alternatively, you can set the JDBC connection
     * URI or a {@code GraphDataseService}.
     *
     * @param dataSource data source
     * @return itself for chaining purposes
     */
    public ConfigurationBuilder withDataSource(DataSource dataSource) {
        this.dataSource = fromNullable(dataSource);
        return this;
    }

    /**
     * Specifies the {@code GraphDataseService} instance.
     * Alternatively, you can set the JDBC connection URI
     * or a {@code DataSource}.
     *
     * @param database GraphDatabaseService instance
     * @return itself for chaining purposes
     */
    public ConfigurationBuilder withGraphDatabaseService(GraphDatabaseService database) {
        this.database = fromNullable(database);
        return this;
    }

    /**
     * Specifies the username allowed to connect to the remote graph database instance.
     * Please be sure to provide a password, if you provide a username, too.
     * @param username username
     * @return itself for chaining purposes
     */
    public ConfigurationBuilder withUsername(String username) {
        this.username = fromNullable(username);
        return this;
    }

    /**
     * Specifies the password allowed to connect to the remote graph database instance.
     * Please be sure to provide a username, if you provide a password, too.
     * @param password password
     * @return itself for chaining purposes
     */
    public ConfigurationBuilder withPassword(String password) {
        this.password = fromNullable(password);
        return this;
    }

    /**
     * @see ConfigurationBuilder#withExecutionContexts(java.util.Collection)
     *
     * @param executionContexts 0 or more Liquigraph execution contexts to allow changeset
     *                          filtering
     * @return itself for chaining purposes
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
     * Sets ClassLoader to use when reading Liquigraph changelogs.
     * Default is <code>Thread.currentThread().getContextClassLoader()</code>.
     * Don't call this unless you REALLY know what you're doing.
     *
     * @param classLoader class loader
     * @return itself for chaining purposes
     */
    public ConfigurationBuilder withClassLoader(ClassLoader classLoader) {
        if (classLoader != null) {
            this.classLoader = classLoader;
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
        Collection<String> errors = newLinkedList();
        errors.addAll(mandatoryOptionValidator.validate(classLoader, masterChangelog));
        errors.addAll(datasourceConnectionValidator.validate(uri, dataSource, database));
        errors.addAll(executionModeValidator.validate(executionMode));
        errors.addAll(userCredentialsOptionValidator.validate(username.orNull(), password.orNull()));

        if (!errors.isEmpty()) {
            throw new RuntimeException(formatErrors(errors));
        }

        muteRestletLogs();
        return new Configuration(
            classLoader,
            masterChangelog,
            dataSourceConfiguration(),
            executionContexts,
            executionMode
        );
    }

    private ConnectionConfiguration dataSourceConfiguration() {
        if (uri.isPresent()) {
            return new ConnectionConfigurationByUri(uri.get(), username, password);
        }

        if (database.isPresent()) {
            return new ConnectionConfigurationByGraphDatabaseService(database.get());
        }

        return new ConnectionConfigurationByDataSource(dataSource.get(), username, password);
    }

    private void muteRestletLogs() {
        Engine.setRestletLogLevel(Level.SEVERE);
    }

    private String formatErrors(Collection<String> errors) {
        String separator = "\n\t - ";
        return format("%s%s", separator, Joiner.on(separator).join(errors));
    }
}
