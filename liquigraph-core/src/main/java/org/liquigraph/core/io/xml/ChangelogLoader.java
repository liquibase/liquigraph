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
 * Loader for changelog files.
 */
public interface ChangelogLoader {

  /**
   * Loads the given changelog.
   *
   * @param changelog the changelog to load
   * @return an input stream of the loaded changelog or <code>null</code> if it does not exists
   * @throws IOException if anything bad happens
   */
  InputStream load(String changelog) throws IOException;
}
