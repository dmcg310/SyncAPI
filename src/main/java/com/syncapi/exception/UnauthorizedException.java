package com.syncapi.exception;

/**
 * Exception thrown when authentication is required but not provided or invalid.
 */
public class UnauthorizedException extends RuntimeException {
    /**
     * Parameterized constructor.
     *
     * @param message the exception message
     */
    public UnauthorizedException(String message) {
        super(message);
    }
}
