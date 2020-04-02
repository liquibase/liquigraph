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
package org.liquigraph.core.io.xml;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class ChainedEntityResolver implements EntityResolver {

    private final List<EntityResolver> resolvers = new ArrayList<>();

    public void addEntityResolver(final EntityResolver resolver) {
        resolvers.add(resolver);
    }

    @Override
    public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
        for (EntityResolver entityResolver : resolvers) {
            InputSource entity = entityResolver.resolveEntity(publicId, systemId);
            if (entity != null) {
                return entity;
            }
        }
        return null;
    }
}
