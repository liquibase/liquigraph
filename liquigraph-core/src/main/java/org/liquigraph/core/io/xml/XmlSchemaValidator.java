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
package org.liquigraph.core.io.xml;

import org.w3c.dom.Node;

import javax.xml.transform.dom.DOMSource;
import java.util.Collection;

import static java.lang.String.format;

public class XmlSchemaValidator {

    /**
     * Validate the fully resolved changelog containing all migrations
     *
     * @param changelog DOM node holding the changelog
     * @throws IllegalArgumentException if there is am XML validation error
     * @return validation error messages
     */
    public Collection<String> validateSchema(Node changelog) {
        return validate(new DOMSource(changelog));
    }

    private Collection<String> validate(DOMSource changelog) {
        try {
            return parse(changelog);
        } catch (Exception e) {
            throw new IllegalArgumentException(
                format("Exception while reading changelog: %n\t%s.", e.getMessage()),
                e
            );
        }
    }

    private Collection<String> parse(DOMSource changelog) throws Exception {
        return new DomSourceValidatorFactory()
                .createValidator(changelog)
                .validate(changelog);
    }

}
