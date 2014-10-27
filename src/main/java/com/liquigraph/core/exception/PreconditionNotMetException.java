package com.liquigraph.core.exception;

public class PreconditionNotMetException extends RuntimeException {
    public PreconditionNotMetException(String message) {
        super(message);
    }
}
