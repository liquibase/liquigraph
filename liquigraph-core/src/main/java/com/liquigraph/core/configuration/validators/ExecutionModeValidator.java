package com.liquigraph.core.configuration.validators;

import com.liquigraph.core.configuration.DryRunMode;
import com.liquigraph.core.configuration.ExecutionMode;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

import static com.liquigraph.core.configuration.RunMode.RUN_MODE;
import static java.lang.String.format;

public class ExecutionModeValidator {

    public Collection<String> validate(ExecutionMode executionMode) {
        if (executionMode == null || executionMode == RUN_MODE) {
            return Collections.emptyList();
        }
        if (!(executionMode instanceof DryRunMode)) {
            throw new IllegalArgumentException("Unknown <executionMode>: " + executionMode);
        }
        return validateDryRunMode((DryRunMode) executionMode);
    }

    private Collection<String> validateDryRunMode(DryRunMode dryRunMode) {
        Collection<String> errors = new LinkedList<>();
        File output = dryRunMode.getOutputFile();
        Path outputDirectory = output.toPath().getParent();
        if (!Files.isDirectory(outputDirectory)) {
            errors.add(format("<%s> is not a directory", outputDirectory));
        }
        if (!Files.isWritable(outputDirectory)) {
            errors.add(format("The directory <%s> must be writable", outputDirectory));
        }
        return errors;
    }

}
