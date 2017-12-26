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
package org.liquigraph.core.configuration.validators;

import java.util.Collection;
import java.util.Collections;

public class UserCredentialsOptionValidator {

    /**
     * Validates the given user credentials in the sense of checking if they are
     * reasonable, i.e. providing both username and password or neither..
     *
     * @param username the username to use to connect
     * @param password the password corresponding to {@code username}
     * @return a collection of Strings describing possible errors, an empty
     * collection if no errors
     */
    public Collection<String> validate(String username, String password) {
        if (username != null ^ password != null) {
            return Collections.singletonList("Please provide both username and password, or none.");
        } else {
            return Collections.emptyList();
        }
    }
}
