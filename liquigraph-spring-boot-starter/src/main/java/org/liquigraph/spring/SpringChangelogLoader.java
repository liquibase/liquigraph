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
package org.liquigraph.spring;

import org.liquigraph.core.io.xml.ChangelogLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import java.io.IOException;
import java.io.InputStream;

import static java.util.Objects.requireNonNull;

/**
 * {@link ChangelogLoader} which uses Springs {@link ResourceLoader} for loading changelogs.
 */
public final class SpringChangelogLoader implements ChangelogLoader {

    private final ResourceLoader resourceLoader;

    public SpringChangelogLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = requireNonNull(resourceLoader, "Resource loader may not be null");
    }

    @Override
    public InputStream load(String changelog) throws IOException {
        final Resource resource = resourceLoader.getResource(changelog);
        if (resource == null) {
            return null;
        }
        return resource.getInputStream();
    }
}
