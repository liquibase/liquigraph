package org.liquigraph.core.configuration;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;

import java.nio.file.Path;

import static java.lang.String.format;

public class ConfigurationBuilderTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Rule
    public TemporaryFolder outputCypherFolder = new TemporaryFolder();

    @Test
    public void fails_on_null_changelog_location() {
        thrown.expect(RuntimeException.class);
        thrown.expectMessage("'masterChangelog' should not be null");

        new ConfigurationBuilder()
            .withUri("http://localhost:7474/db/data")
            .build();
    }

    @Test
    public void fails_on_non_existing_changelog() {
        thrown.expect(RuntimeException.class);
        thrown.expectMessage("'masterChangelog' points to a non-existing location: /non-existing.xml");

        new ConfigurationBuilder()
            .withUri("http://localhost:7474/db/data")
            .withMasterChangelogLocation("/non-existing.xml")
            .build();
    }

    @Test
    public void fails_on_null_uri() {
        thrown.expect(RuntimeException.class);
        thrown.expectMessage("'uri' should not be null");

        new ConfigurationBuilder()
            .withMasterChangelogLocation("/changelog.xml")
            .build();
    }

    @Test
    public void fails_on_unsupported_protocol() {
        thrown.expect(RuntimeException.class);
        thrown.expectMessage(
            "\t - Invalid JDBC URI. Supported configurations:\n" +
            "\t - jdbc:neo4j://<host>:<port>/\n" +
            "\t - jdbc:neo4j:file:/path/to/db\n" +
            "\t - jdbc:neo4j:mem or jdbc:neo4j:mem:name.\n" +
            "Given: ssh://sorry@buddy"
        );

        new ConfigurationBuilder()
            .withMasterChangelogLocation("/changelog.xml")
            .withUri("ssh://sorry@buddy")
            .build();
    }

    @Test
    public void add_ups_all_misconfiguration_errors() {
        thrown.expect(RuntimeException.class);
        thrown.expectMessage(" - 'masterChangelog' should not be null");
        thrown.expectMessage(" - 'uri' should not be null");

        new ConfigurationBuilder().build();
    }

    @Test
    public void path_should_point_to_a_directory() throws Exception {
        outputCypherFolder.create();
        Path path = outputCypherFolder.newFile("output.cypher").toPath();

        thrown.expect(RuntimeException.class);
        thrown.expectMessage(format("<%s> is not a directory", path.toString()));

        new ConfigurationBuilder()
                    .withMasterChangelogLocation("/changelog.xml")
                    .withUri("file:///sorry@buddy")
                    .withDryRunMode(path)
                    .build();
    }

    @Test
    public void output_folder_must_be_writable() throws Exception {
        outputCypherFolder.create();
        Path path = outputCypherFolder.getRoot().toPath();
        path.toAbsolutePath().toFile().setWritable(false);

        thrown.expect(RuntimeException.class);
        thrown.expectMessage(format("<%s> must be writable", path.toString()));

        new ConfigurationBuilder()
                    .withMasterChangelogLocation("/changelog.xml")
                    .withUri("http:///sorry@buddy")
                    .withDryRunMode(path)
                    .build();
    }
}