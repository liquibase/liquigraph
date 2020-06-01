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
package org.liquigraph.maven;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;

import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.project.MavenProject;
import org.liquigraph.core.io.xml.ChangelogLoader;
import org.liquigraph.core.io.xml.ClassLoaderChangelogLoader;

class ProjectChangelogLoader {

    public static ChangelogLoader getChangelogLoader(MavenProject project) throws DependencyResolutionRequiredException, MalformedURLException {
        List<String> classPathElements = project.getCompileClasspathElements();
        int size = classPathElements.size();
        URL[] urls = new URL[size];
        for (int i = 0; i < size; i++) {
            urls[i] = new File(classPathElements.get(i)).toURI().toURL();
        }
        return new ClassLoaderChangelogLoader(new URLClassLoader(urls, Thread.currentThread().getContextClassLoader()));
    }
}
