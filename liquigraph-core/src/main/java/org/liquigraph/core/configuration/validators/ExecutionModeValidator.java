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
package org.liquigraph.core.configuration.validators;

import org.liquigraph.core.configuration.DryRunMode;
import org.liquigraph.core.configuration.ExecutionMode;
import org.liquigraph.core.configuration.RunMode;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

import static java.lang.String.format;

public class ExecutionModeValidator {

    public Collection<String> validate(ExecutionMode executionMode) {
        if (executionMode == null || executionMode == RunMode.RUN_MODE) {
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
