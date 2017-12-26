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
package org.liquigraph.maven;

import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

public class ProjectClassLoader {

    public static ClassLoader getClassLoader(MavenProject project) throws DependencyResolutionRequiredException, MalformedURLException {
        List<String> classPathElements = compileClassPathElements(project);
        List<URL> classpathElementUrls = new ArrayList<>(classPathElements.size());
        for (String classPathElement : classPathElements) {
            classpathElementUrls.add(new File(classPathElement).toURI().toURL());
        }
        return new URLClassLoader(
            classpathElementUrls.toArray(new URL[classpathElementUrls.size()]),
            Thread.currentThread().getContextClassLoader()
        );
    }

    @SuppressWarnings("unchecked")
    private static List<String> compileClassPathElements(MavenProject project) throws DependencyResolutionRequiredException {
        return newArrayList(project.getCompileClasspathElements());
    }
}
