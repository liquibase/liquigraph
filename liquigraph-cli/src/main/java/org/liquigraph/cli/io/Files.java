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
package org.liquigraph.cli.io;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import static org.liquigraph.core.exception.Throwables.propagate;

public class Files {

    public static URL toUrl(String location) {
        try {
            return new File(location).toURI().toURL();
        }
        catch (MalformedURLException e) {
            throw propagate(e);
        }
    }
}
