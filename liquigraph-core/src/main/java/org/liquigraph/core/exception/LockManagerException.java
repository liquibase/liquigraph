package org.liquigraph.core.exception;

public class LockManagerException extends RuntimeException {

    public LockManagerException(String message) {
        super(message);
    }
    public LockManagerException(String message, Throwable cause) {
        super(message, cause);
    }
}
