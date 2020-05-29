/*
 * Copyright 2014-2022 the original author or authors.
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
package org.liquigraph.cli.io;

import java.net.URL;
import java.net.URLClassLoader;

public class ClassLoaders {

    public static ClassLoader urlClassLoader(URL url, URL... rest) {
        return new URLClassLoader(toArray(url, rest));
    }

    private static URL[] toArray(URL url, URL[] rest) {
        URL[] urls = new URL[1 + rest.length];
        urls[0] = url;
        System.arraycopy(rest, 0, urls, 1, rest.length);
        return urls;
    }
}
