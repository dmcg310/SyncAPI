package com.syncapi.util;

/**
 * Enum representing HTTP request methods.
 */
public enum RequestMethod {
    GET,
    POST,
    PUT,
    PATCH,
    DELETE;

    /**
     * Converts a string to a RequestMethod enum value.
     *
     * @param method the method string
     * @return the corresponding RequestMethod
     * @throws IllegalArgumentException if the method is invalid
     */
    public static RequestMethod fromString(String method) {
        for (RequestMethod rm : RequestMethod.values()) {
            if (rm.name().equalsIgnoreCase(method)) {
                return rm;
            }
        }

        throw new IllegalArgumentException("Invalid request method: " + method);
    }
}
