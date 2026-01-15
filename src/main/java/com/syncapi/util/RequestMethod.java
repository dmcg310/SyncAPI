package com.syncapi.util;

public enum RequestMethod {
    GET,
    POST,
    PUT,
    PATCH,
    DELETE;

    public static RequestMethod fromString(String method) {
        for (RequestMethod rm : RequestMethod.values()) {
            if (rm.name().equalsIgnoreCase(method)) {
                return rm;
            }
        }

        throw new IllegalArgumentException("Invalid request method: " + method);
    }
}
