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

import org.junit.Test;
import org.liquigraph.core.io.xml.ChangelogLoader;
import org.liquigraph.core.io.xml.ClassLoaderChangelogLoader;

import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;

public class MandatoryOptionValidatorTest {

    private MandatoryOptionValidator validator = new MandatoryOptionValidator();

    private ChangelogLoader changelogLoader = ClassLoaderChangelogLoader.currentThreadContextClassLoader();

    @Test
    public void fails_on_invalid_changelog_location() {
        Collection<String> errors = validator.validate(
            changelogLoader,
            "unlikelytobefoundchangelog.xml"
        );

        assertThat(errors).containsExactly(
            "'masterChangelog' points to a non-existing location: unlikelytobefoundchangelog.xml"
        );
    }

}
