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
package org.liquigraph.core.configuration.validators;

import com.google.common.collect.Lists;
import java.util.Collection;
import java.util.LinkedList;

public class UserCredentialsOptionValidator {

    public Collection<String> validate(String username, String password) {
        Collection<String> errors = new LinkedList<>();
        errors.addAll(validateUserCredentials(username, password));
        return errors;
    }

    private static Collection<String> validateUserCredentials(String username, String password) {
        boolean passwordButNoUsername = password != null && username == null;
        boolean usernameButNoPassword = password == null && username != null;
        
        if(passwordButNoUsername) {
            return Lists.newArrayList(String.format("Please provide a username corresponding to the password <%s>.", password));
        } else if (usernameButNoPassword) {
            return Lists.newArrayList(String.format("Please provide a password for username <%s>.", username));
        } else {
            return Lists.newArrayList();
        }
    }

}
