package com.liquigraph.core.exception;

import static java.lang.String.format;

public class PreconditionSyntaxException extends RuntimeException {

    public PreconditionSyntaxException(Throwable cause, String message, Object... arguments) {
        super(format(message, arguments), cause);
    }

    public PreconditionSyntaxException(String message, Object... arguments) {
        super(format(message, arguments));
    }
}
