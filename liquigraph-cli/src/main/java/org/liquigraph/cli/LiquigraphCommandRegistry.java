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
package org.liquigraph.cli;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import org.liquigraph.cli.commands.LiquigraphCommand;
import org.liquigraph.core.api.LiquigraphApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class LiquigraphCommandRegistry {

    @Parameter(
        names = {"--help", "-h"},
        description = "Get this help",
        help = true
    )
    private boolean help;

    @Parameter(
        names = {"--version", "-v"},
        description = "Show the version",
        help = true
    )
    private boolean version;

    private final Map<String, LiquigraphCommand> registry = new LinkedHashMap<>();

    public LiquigraphCommandRegistry registerCommand(String name, LiquigraphCommand liquigraphCommand) {
        registry.put(name, liquigraphCommand);
        return this;
    }

    public Optional<LiquigraphCommand> resolve(String[] args) {
        JCommander parser = buildParser();

        parser.parse(args);

        if (this.version && this.help) {
            throw new RuntimeException("Either --version or --help must be set, not both");
        }
        if (this.version) {
            return Optional.of(new VersionCommand());
        }
        if (this.help) {
            return Optional.of(new HelpCommand(parser));
        }
        return Optional.ofNullable(registry.get(parser.getParsedCommand()));
    }

    private JCommander buildParser() {
        JCommander jCommander = new JCommander();
        jCommander.setProgramName("liquigraph");
        jCommander.addObject(this);
        registry.forEach(jCommander::addCommand);
        return jCommander;
    }

}

class VersionCommand implements LiquigraphCommand {

    private static final Logger LOGGER = LoggerFactory.getLogger(VersionCommand.class);

    @Override
    public void accept(LiquigraphApi ignored) {
        printVersion();
    }

    private static void printVersion() {
        Optional<String> version = getVersion();
        System.out.println(version.orElse("Unknown version!"));
    }

    private static Optional<String> getVersion() {
        try (InputStream propsIs = LiquigraphCli.class.getResourceAsStream("/liquigraph-cli.properties")) {
            if (propsIs != null) {
                Properties props = new Properties();
                props.load(propsIs);
                return Optional.ofNullable(props.getProperty("liquigraph.version"));
            }
        }
        catch (IOException e) {
            LOGGER.error("An exception occurred while loading the properties", e);
        }
        return Optional.empty();
    }
}

class HelpCommand implements LiquigraphCommand {

    private final JCommander commander;

    HelpCommand(JCommander commander) {
        this.commander = commander;
    }

    @Override
    public void accept(LiquigraphApi ignored) {
        this.commander.usage();
    }
}



