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

import java.io.IOException;
import java.io.InputStream;

/**
 * A {@link ChangelogLoader} which uses a {@link ClassLoader} to load changelogs.
 */
public final class ClassLoaderChangelogLoader implements ChangelogLoader {

  private final ClassLoader classLoader;

  public ClassLoaderChangelogLoader(ClassLoader classLoader) {
    this.classLoader = classLoader;
  }

  public ClassLoader getClassLoader() {
    return classLoader;
  }

  @Override
  public InputStream load(String changelog) throws IOException {
    return classLoader.getResourceAsStream(changelog);
  }

  public static ClassLoaderChangelogLoader currentThreadContextClassLoader() {
    return new ClassLoaderChangelogLoader(Thread.currentThread().getContextClassLoader());
  }
}
