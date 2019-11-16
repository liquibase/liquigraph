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

import java.lang.invoke.MethodHandles;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;

/**
 * Resolves XML entities against local classpath resources.
 */
public class LiquigraphLocalEntityResolver implements EntityResolver {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private static final Map<String, String> KNOWN_ENTITIES = new HashMap<>();

    static {
        KNOWN_ENTITIES.put("http://www.liquigraph.org/schema/1.0/liquigraph.xsd", "schema/1.0/liquigraph.xsd");
        KNOWN_ENTITIES.put("https://www.liquigraph.org/schema/1.0/liquigraph.xsd", "schema/1.0/liquigraph.xsd");
        KNOWN_ENTITIES.put("http://liquigraph.github.io/schema/1.0/liquigraph.xsd", "schema/1.0/liquigraph.xsd");
        KNOWN_ENTITIES.put("https://liquigraph.github.io/schema/1.0/liquigraph.xsd", "schema/1.0/liquigraph.xsd");
        KNOWN_ENTITIES.put("http://fbiville.github.io/liquigraph/schema/1.0/liquigraph.xsd",
        "schema/1.0/liquigraph.xsd");
        KNOWN_ENTITIES.put("https://fbiville.github.io/liquigraph/schema/1.0/liquigraph.xsd",
        "schema/1.0/liquigraph.xsd");
        KNOWN_ENTITIES.put("http://www.liquigraph.org/schema/1.0-RC3/liquigraph.xsd", "schema/1.0-RC3/liquigraph.xsd");
        KNOWN_ENTITIES.put("https://www.liquigraph.org/schema/1.0-RC3/liquigraph.xsd", "schema/1.0-RC3/liquigraph.xsd");
        KNOWN_ENTITIES.put("http://fbiville.github.io/liquigraph/schema/1.0-RC3/liquigraph.xsd",
        "schema/1.0-RC3/liquigraph.xsd");
        KNOWN_ENTITIES.put("https://fbiville.github.io/liquigraph/schema/1.0-RC3/liquigraph.xsd",
        "schema/1.0-RC3/liquigraph.xsd");
        KNOWN_ENTITIES.put("http://www.liquigraph.org/schema/changelog.xsd", "schema/changelog.xsd");
        KNOWN_ENTITIES.put("https://www.liquigraph.org/schema/changelog.xsd", "schema/changelog.xsd");
    }

    @Override
    public InputSource resolveEntity(String publicId, final String systemId) {
        LOGGER.trace("Resolving entity, public {}, system {}", publicId, systemId);
        if (KNOWN_ENTITIES.containsKey(systemId)) {
            return new InputSource(getClass().getClassLoader().getResourceAsStream(KNOWN_ENTITIES.get(systemId)));
        }
        return null;
    }

}
