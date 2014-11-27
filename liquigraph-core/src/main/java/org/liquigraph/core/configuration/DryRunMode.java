package org.liquigraph.core.configuration;

import java.io.File;
import java.nio.file.Path;

public final class DryRunMode implements ExecutionMode {

    private final File outputFile;

    public DryRunMode(Path directory) {
        this.outputFile = new File(directory.toString(), "output.cypher");
    }

    public File getOutputFile() {
        return outputFile;
    }
}
