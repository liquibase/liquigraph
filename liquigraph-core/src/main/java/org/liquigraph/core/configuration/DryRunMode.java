/*
 * Copyright 2014-2021 the original author or authors.
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
package org.liquigraph.core.configuration;

import java.io.File;
import java.nio.file.Path;
import java.util.Objects;

public final class DryRunMode implements ExecutionMode {

    private final File outputFile;

    public DryRunMode(Path directory) {
        this.outputFile = new File(directory.toString(), "output.cypher");
    }

    public File getOutputFile() {
        return outputFile;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DryRunMode that = (DryRunMode) o;
        return Objects.equals(outputFile, that.outputFile);
    }

    @Override
    public int hashCode() {
        return Objects.hash(outputFile);
    }

    @Override
    public String toString() {
        return "DryRunMode{" +
            "outputFile=" + outputFile +
            '}';
    }
}
