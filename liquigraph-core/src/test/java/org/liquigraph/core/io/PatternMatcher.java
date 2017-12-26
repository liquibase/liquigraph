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
package org.liquigraph.core.io;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;

import java.util.regex.Pattern;

class PatternMatcher extends BaseMatcher<String> {

    private final Pattern pattern;

    private PatternMatcher(String pattern) {
        this.pattern = Pattern.compile(pattern);
    }

    public static BaseMatcher<String> matchesPattern(String pattern) {
        return new PatternMatcher(pattern);
    }

    @Override
    public boolean matches(Object item) {
        if (!(item instanceof String)) {
            throw new IllegalArgumentException("Can only match patterns on Strings");
        }
        String string = (String) item;
        return pattern.matcher(string).matches();
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("matchesPattern should match pattern ").appendValue(pattern.pattern());
    }
}
