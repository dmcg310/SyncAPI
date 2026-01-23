package com.syncapi.exception;

/**
 * Exception thrown when access to a resource is denied.
 */
public class AccessDeniedException extends RuntimeException {
    /**
     * Parameterized constructor.
     *
     * @param message the exception message
     */
    public AccessDeniedException(String message) {
        super(message);
    }
}
