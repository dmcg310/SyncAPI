package com.syncapi.exception;

/**
 * Exception thrown when a requested resource is not found.
 */
public class ResourceNotFoundException extends RuntimeException {
    /**
     * Parameterized constructor.
     *
     * @param message the exception message
     */
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
