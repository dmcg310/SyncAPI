package com.syncapi.exception;

/**
 * Exception thrown when a request is invalid or malformed.
 */
public class BadRequestException extends RuntimeException {
    /**
     * Parameterized constructor.
     *
     * @param message the exception message
     */
    public BadRequestException(String message) {
        super(message);
    }
}
