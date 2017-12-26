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
package org.liquigraph.core.configuration;

import com.google.common.base.Supplier;
import org.liquigraph.core.io.ChangelogFileWriter;
import org.liquigraph.core.io.ChangelogGraphWriter;
import org.liquigraph.core.io.ChangelogWriter;
import org.liquigraph.core.io.ConditionExecutor;
import org.liquigraph.core.io.ConditionPrinter;
import org.liquigraph.core.io.xml.ChangelogLoader;
import org.liquigraph.core.io.xml.ClassLoaderChangelogLoader;

import java.sql.Connection;

/**
 * Immutable Liquigraph configuration settings.
 * Please see {@link ConfigurationBuilder} to
 * create a configuration instance tailored to your environment.
 *
 * @see ConfigurationBuilder
 * @see ConnectionConfiguration
 */
public final class Configuration {

    private final ChangelogLoader changelogLoader;
    private final String masterChangelog;
    private final ConnectionConfiguration connectionConfiguration;
    private final ExecutionContexts executionContexts;
    private final ExecutionMode executionMode;

    Configuration(ChangelogLoader changelogLoader,
                  String masterChangelog,
                  ConnectionConfiguration connectionConfiguration,
                  ExecutionContexts executionContexts,
                  ExecutionMode executionMode) {

        this.changelogLoader = changelogLoader;
        this.masterChangelog = masterChangelog;
        this.connectionConfiguration = connectionConfiguration;
        this.executionContexts = executionContexts;
        this.executionMode = executionMode;
    }

    /**
     * @deprecated Use {@link #changelogLoader()} for loading any kind of changelog.
     */
    @Deprecated
    public ClassLoader classLoader() {
        if (changelogLoader instanceof ClassLoaderChangelogLoader) {
            return ((ClassLoaderChangelogLoader) changelogLoader).getClassLoader();
        } else {
            return null;
        }
    }

    public ChangelogLoader changelogLoader() {
        return changelogLoader;
    }

    public String masterChangelog() {
        return masterChangelog;
    }

    public ConnectionConfiguration dataSourceConfiguration() {
        return connectionConfiguration;
    }

    public ExecutionContexts executionContexts() {
        return executionContexts;
    }

    public ExecutionMode executionMode() {
        return executionMode;
    }

    public ChangelogWriter resolveWriter(Connection writeConnection,
                                         Supplier<Connection> connectionSupplier,
                                         ConditionExecutor conditionExecutor,
                                         ConditionPrinter conditionPrinter) {

        ExecutionMode executionMode = executionMode();
        if (executionMode == RunMode.RUN_MODE) {
            return new ChangelogGraphWriter(writeConnection, connectionSupplier, conditionExecutor);
        }
        if (executionMode instanceof DryRunMode) {
            DryRunMode dryRunMode = (DryRunMode) executionMode;
            return new ChangelogFileWriter(conditionPrinter, dryRunMode.getOutputFile());
        }
        throw new IllegalStateException("Unsupported <executionMode>: " + executionMode);
    }
}
