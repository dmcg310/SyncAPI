package com.syncapi.dto.request;

import java.util.Map;

/**
 * DTO containing a request with substituted variables.
 */
public class SubstitutedRequest {
    private final String url;
    private final Map<String, String> headers;
    private final Map<String, Object> body;

    /**
     * Parameterized constructor.
     *
     * @param url     the substituted URL
     * @param headers the substituted headers
     * @param body    the substituted body
     */
    public SubstitutedRequest(String url, Map<String, String> headers, Map<String, Object> body) {
        this.url = url;
        this.headers = headers;
        this.body = body;
    }

    public String getUrl() {
        return url;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public Map<String, Object> getBody() {
        return body;
    }
}
