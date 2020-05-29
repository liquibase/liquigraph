/*
 * Copyright 2014-2022 the original author or authors.
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

import org.liquigraph.cli.commands.DryRun;
import org.liquigraph.cli.commands.Run;
import org.liquigraph.core.api.Liquigraph;

import java.util.Arrays;

public final class LiquigraphCli {

    private final Liquigraph liquigraph;

    private final LiquigraphCommandRegistry registry;

    public LiquigraphCli(Liquigraph liquigraph) {
        this.liquigraph = liquigraph;
        this.registry = new LiquigraphCommandRegistry()
        .registerCommand("dry-run", new DryRun())
        .registerCommand("run", new Run());
    }

    public static void main(String[] args) {
        LiquigraphCli cli = new LiquigraphCli(new Liquigraph());
        cli.execute(args);
    }

    // visible for testing
    void execute(String[] args) {
        registry
        .resolve(args)
        .orElseThrow(() -> new RuntimeException(
        String.format("Could not resolve command. Given args: %s%n", Arrays.toString(args))
        ))
        .accept(this.liquigraph);
    }
}
