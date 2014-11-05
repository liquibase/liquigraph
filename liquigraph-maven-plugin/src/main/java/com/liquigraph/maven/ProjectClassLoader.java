package com.liquigraph.maven;

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

    private static List<String> compileClassPathElements(MavenProject project) throws DependencyResolutionRequiredException {
        return newArrayList(project.getCompileClasspathElements());
    }
}
