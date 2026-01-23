package com.syncapi.exception;

/**
 * Exception thrown when a conflict occurs.
 */
public class ConflictException extends RuntimeException {
    /**
     * Parameterized constructor.
     *
     * @param message the exception message
     */
    public ConflictException(String message) {
        super(message);
    }
}
