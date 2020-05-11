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
package org.liquigraph.core.api;

import org.liquigraph.core.configuration.Configuration;

/**
 * Runs a migration
 *
 * Runner concrete subclass will implement the actions for a given
 * executionMode.
 */
public interface Runner {

    /**
     * Execute the migrations specified by configuration parameter
     *
     * @param configuration contains all the configuration data needed for running this migration
     */
    void runMigrations(Configuration configuration);
}
