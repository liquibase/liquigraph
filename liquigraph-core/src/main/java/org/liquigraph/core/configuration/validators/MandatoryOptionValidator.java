package org.liquigraph.core.configuration.validators;

import org.neo4j.jdbc.Driver;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.LinkedList;

import static java.lang.String.format;

public class MandatoryOptionValidator {

    public Collection<String> validate(ClassLoader classLoader, String masterChangelog, String uri) {
        Collection<String> errors = new LinkedList<>();
        errors.addAll(validateMasterChangelog(masterChangelog, classLoader));
        errors.addAll(validateGraphInstance(uri));
        return errors;
    }

    private static Collection<String> validateMasterChangelog(String masterChangelog, ClassLoader classLoader) {
        Collection<String> errors = new LinkedList<>();
        if (masterChangelog == null) {
            errors.add("'masterChangelog' should not be null");
        }
        else {
            try (InputStream stream = classLoader.getResourceAsStream(masterChangelog)) {
                if (stream == null) {
                    errors.add(format("'masterChangelog' points to a non-existing location: %s", masterChangelog));
                }
            } catch (IOException e) {
                errors.add(format("'masterChangelog' read error. Cause: %s", e.getMessage()));
            }
        }
        return errors;
    }

    private static Collection<String> validateGraphInstance(String uri) {
        Collection<String> errors = new LinkedList<>();
        if (uri == null) {
            errors.add("'uri' should not be null");
        }
        else if (!uri.startsWith(Driver.CON_PREFIX)) {
            errors.add(format("Invalid JDBC URI. Supported configurations:%n" +
                "\t - jdbc:neo4j://<host>:<port>/%n" +
                "\t - jdbc:neo4j:file:/path/to/db%n" +
                "\t - jdbc:neo4j:mem or jdbc:neo4j:mem:name.%n" +
                "Given: %s", uri
            ));
        }
        return errors;
    }

}
