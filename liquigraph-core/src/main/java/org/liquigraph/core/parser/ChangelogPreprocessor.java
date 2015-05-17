package org.liquigraph.core.parser;

import org.w3c.dom.Node;

public class ChangelogPreprocessor {

    private final ImportResolver resolver;

    public ChangelogPreprocessor(ImportResolver resolver) {
        this.resolver = resolver;
    }

    public Node preProcess(String changelogPath, ClassLoader classLoader) {
        return resolver.resolveImports(changelogPath, classLoader);
    }
}
