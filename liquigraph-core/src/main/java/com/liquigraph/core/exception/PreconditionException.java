package com.liquigraph.core.exception;

import static java.lang.String.format;

public class PreconditionException extends RuntimeException {

    public PreconditionException(Throwable cause, String message, Object... arguments) {
        super(format(message, arguments), cause);
    }

    public PreconditionException(String message, Object... arguments) {
        super(format(message, arguments));
    }
}
