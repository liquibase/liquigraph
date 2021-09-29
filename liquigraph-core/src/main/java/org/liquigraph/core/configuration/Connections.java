/*
 * Copyright 2014-2021 the original author or authors.
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

import javax.sql.DataSource;
import java.util.Optional;

public class Connections {

    public static ConnectionConfiguration provide(Optional<String> uri,
                                                  Optional<String> database,
                                                  Optional<String> username,
                                                  Optional<String> password,
                                                  Optional<DataSource> dataSource) {
        if (uri.isPresent()) {
            return new ConnectionConfigurationByUri(uri.get(), database, username, password);
        }
        return new ConnectionConfigurationByDataSource(dataSource.get(), username, password);
    }
}
