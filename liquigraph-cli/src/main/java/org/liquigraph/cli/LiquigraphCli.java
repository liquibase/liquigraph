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
package org.liquigraph.cli;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import org.liquigraph.core.api.Liquigraph;
import org.liquigraph.core.configuration.ConfigurationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;
import static org.liquigraph.core.exception.Throwables.propagate;

public class LiquigraphCli {
    private static final Logger LOGGER = LoggerFactory.getLogger(LiquigraphCli.class);

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

    @Parameter(
            names = {"--changelog", "-c"},
            description = "Master Liquigraph changelog location.\n" +
                    "\t Prefix with 'classpath:' if location is in classpath",
            required = true
    )
    private String changelog;

    @Parameter(
            names = {"--graph-db-uri", "-g"},
            description = "Graph JDBC URI:\n" +
                    "\t- jdbc:neo4j:http://host:port/\n" +
                    "\t- jdbc:neo4j:https://host:port/\n" +
                    "\t- jdbc:neo4j:bolt://host:port/\n",
            required = true
    )
    private String graphDbUri;

    @Parameter(
            names = {"--database", "-db"},
            description = "Graph DB database (remote only)"
    )
    private String database;

    @Parameter(
            names = {"--username", "-u"},
            description = "Graph DB username (remote only)"
    )
    private String username;

    @Parameter(
            names = {"--password", "-p"},
            description = "Graph DB password (remote only)",
            password = true
    )
    private String password;

    @Parameter(
            names = {"--execution-contexts", "-x"},
            description = "Comma-separated list of Liquigraph execution contexts"
    )
    private String executionContexts = "";

    @Parameter(
            names = {"--dry-run-output-directory", "-d"},
            description = "Output directory path into which changeset queries will be written. " +
                    "Not setting this option will trigger RUN mode."
    )
    private String dryRunOutputDirectory;

    @Parameter(description = "[action] \n" +
            "  action: Action to be executed. " +
            "Accepted values are: " +
            "run (default if --dry-run-output-directory is not set), " +
            "dry-run (default if --dry-run-output-directory is set), " +
            "clear-checksum"
    )
    private List<String> parameters = new ArrayList<>();

    public static void main(String[] args) {
        LiquigraphCli cli = new LiquigraphCli();
        JCommander commander = new JCommander(cli);
        commander.parse(args);
        commander.setProgramName("liquigraph");

        if (cli.help) {
            commander.usage();
            return;
        }

        if (cli.version) {
            printVersion();
            return;
        }

        ConfigurationBuilder builder = new ConfigurationBuilder()
                .withMasterChangelogLocation(fileName(cli.changelog))
                .withUri(cli.graphDbUri)
                .withDatabase(cli.database)
                .withUsername(cli.username)
                .withPassword(cli.password)
                .withExecutionContexts(executionContexts(cli.executionContexts))
                .withClassLoader(classloader(parentFolder(cli.changelog), cli.dryRunOutputDirectory));

        Action action = action(cli);

        if (action == Action.CLEAR_CHECKSUM) {
            new Liquigraph().clearChecksums(builder.build());
        } else {
            if (action == Action.DRY_RUN) {
                builder.withDryRunMode(Paths.get(cli.dryRunOutputDirectory));
            } else if (action == Action.RUN) {
                builder.withRunMode();
            }
            new Liquigraph().runMigrations(builder.build());
        }
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
        } catch (IOException e) {
            LOGGER.error("An exception occurred while loading the properties", e);
        }
        return Optional.empty();
    }

    private static String fileName(String changelog) {
        return new File(changelog).getName();
    }

    private static String parentFolder(String changelog) {
        try {
            return new File(changelog).getCanonicalFile().getParent();
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private static Collection<String> executionContexts(String executionContexts) {
        if (executionContexts.isEmpty()) {
            return emptyList();
        }
        Collection<String> result = new ArrayList<>();
        for (String context : executionContexts.split(",")) {
            result.add(context.trim());
        }
        return result;
    }

    private static ClassLoader classloader(String changelog, String dryRunOutputDirectory) {
        List<URL> resources = new ArrayList<>();
        try {
            resources.add(toUrl(changelog));
            if (dryRunOutputDirectory != null) {
                resources.add(toUrl(dryRunOutputDirectory));
            }
        } catch (MalformedURLException e) {
            throw propagate(e);
        }

        return new URLClassLoader(resources.toArray(new URL[0]));
    }

    private static URL toUrl(String location) throws MalformedURLException {
        return new File(location).toURI().toURL();
    }

    private static Action action(LiquigraphCli cli) {
        List<String> actionNames = cli.parameters.stream()
            .filter(it -> !it.startsWith("--"))
            .collect(Collectors.toList());
        if (actionNames.size() == 1) {
            String actionName = actionNames.get(0);
            return Action.fromName(actionName)
            .orElseThrow(
                () -> new ParameterException(
                    String.format("Parameter action '%s' " +
                        "is invalid, must be one of %s",
                        actionName,
                        Action.actions().stream().collect(Collectors.joining(",")))
                    )
            );
        } else if (actionNames.isEmpty()) {
            return cli.dryRunOutputDirectory != null ? Action.DRY_RUN : Action.RUN;
        } else {
            throw new ParameterException(
                "Multiple actions where found: " +
                cli.parameters +
                " Please specify only one action."
            );
        }
    }

    enum Action {
        DRY_RUN("dry-run"),
        RUN("run"),
        CLEAR_CHECKSUM("clear-checksum");

        private String name;

        Action(String name) {
            this.name = name;
        }

        public static List<String> actions() {
            return Arrays.stream(Action.values()).map(Action::name).collect(Collectors.toList());
        }

        public static Optional<Action> fromName(String name) {
            return Arrays.stream(Action.values())
                    .filter(it -> it.name.equals(name))
                    .findAny();
        }
    }
}
