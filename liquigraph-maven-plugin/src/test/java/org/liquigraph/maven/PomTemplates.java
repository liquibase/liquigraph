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
package org.liquigraph.maven;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Map;

public class PomTemplates {

    public static File substitute(File inputFile, Map<String, String> substitutions) {
        String contents = readInput(inputFile);
        String updatedContents = substitute(substitutions, contents);
        File newFile = new File(inputFile.getParent(), "pom.xml");
        writeOutput(newFile, updatedContents);
        return newFile;
    }

    private static String readInput(File inputFile) {
        try {
            return Files.readString(inputFile.toPath(), StandardCharsets.UTF_8);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static String substitute(Map<String, String> substitutions, String contents) {
        for (Map.Entry<String, String> entry : substitutions.entrySet()) {
            contents = contents.replace(entry.getKey(), entry.getValue());
        }
        return contents;
    }

    private static void writeOutput(File newFile, String contents) {
        try {
            Files.writeString(newFile.toPath(), contents, StandardCharsets.UTF_8);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
