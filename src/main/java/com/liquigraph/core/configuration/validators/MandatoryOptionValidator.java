package com.liquigraph.core.configuration.validators;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.LinkedList;

import static java.lang.String.format;

public class MandatoryOptionValidator {

    public Collection<String> validate(String masterChangelog, String uri) {
        Collection<String> errors = new LinkedList<>();
        errors.addAll(validateMasterChangelog(masterChangelog));
        errors.addAll(validateUri(uri));
        return errors;
    }

    private static Collection<String> validateMasterChangelog(String masterChangelog) {
        Collection<String> errors = new LinkedList<>();
        if (masterChangelog == null) {
            errors.add("masterChangelog should not be null");
        }
        else {
            try (InputStream stream = MandatoryOptionValidator.class.getResourceAsStream(masterChangelog)) {
                if (stream == null) {
                    errors.add(format("masterChangelog points to a non-existing location: %s", masterChangelog));
                }
            } catch (IOException e) {
                errors.add(format("masterChangelog read error. Cause: %s", e.getMessage()));
            }
        }
        return errors;
    }

    private static Collection<String> validateUri(String uri) {
        Collection<String> errors = new LinkedList<>();
        if (uri == null) {
            errors.add("'uri' should not be null");
        }
        else if (!uri.startsWith("file:") && !uri.startsWith("http://") && !uri.startsWith("https://")) {
            errors.add(format("'uri' supports only 'file', 'http' and 'https' protocols. Given: %s", uri));
        }
        else {
            try {
                new URI(uri);
            } catch (URISyntaxException e) {
                errors.add(format("'uri' is malformed. Given: %s", uri));
            }
        }
        return errors;
    }
}
