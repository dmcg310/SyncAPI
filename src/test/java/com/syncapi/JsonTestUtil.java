package com.syncapi;

import org.apache.commons.lang3.tuple.Pair;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

/**
 * Utility class for JSON serialization and HTTP request building in tests.
 */
public final class JsonTestUtil {
    public static final String AUTH_HEADER = "Authorization";
    public static final String CONTENT_TYPE_HEADER = "Content-Type";
    public static final String CONTENT_TYPE_JSON = "application/json";
    public static final String BEARER_PREFIX = "Bearer ";
    public static final String BASIC_PREFIX = "Basic ";

    public static final int SUCCESS_STATUS_CODE = 200;
    public static final int BAD_STATUS_CODE = 0;

    public static final String SUCCESS_STATUS_TEXT = "200 OK";

    private static final MediaType JSON_MEDIA_TYPE = MediaType.APPLICATION_JSON;

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private JsonTestUtil() {
    }

    /**
     * Converts an object to its JSON string representation.
     */
    public static String toJson(Object object) {
        try {
            return OBJECT_MAPPER.writeValueAsString(object);
        } catch (JacksonException e) {
            throw new RuntimeException("Failed to serialize object to JSON", e);
        }
    }

    /**
     * Creates a POST request with JSON body (no auth).
     */
    public static MockHttpServletRequestBuilder postJson(String url, Object body) {
        return post(url)
                .contentType(JSON_MEDIA_TYPE)
                .accept(JSON_MEDIA_TYPE)
                .content(toJson(body));
    }

    /**
     * Creates a GET request with JSON accept header.
     */
    public static MockHttpServletRequestBuilder getJson(String url) {
        return get(url)
                .accept(JSON_MEDIA_TYPE);
    }

    /**
     * Creates a GET request with Bearer token authentication.
     */
    public static MockHttpServletRequestBuilder getJsonAuth(String url, String token) {
        return get(url)
                .header(AUTH_HEADER, BEARER_PREFIX + token)
                .accept(JSON_MEDIA_TYPE);
    }

    /**
     * Creates a POST request with JSON body and Bearer token authentication.
     */
    public static MockHttpServletRequestBuilder postJsonAuth(String url, Object body, String token) {
        return post(url)
                .header(AUTH_HEADER, BEARER_PREFIX + token)
                .contentType(JSON_MEDIA_TYPE)
                .accept(JSON_MEDIA_TYPE)
                .content(toJson(body));
    }

    /**
     * Creates a PUT request with JSON body and Bearer token authentication.
     */
    public static MockHttpServletRequestBuilder putJsonAuth(String url, Object body, String token) {
        return put(url)
                .header(AUTH_HEADER, BEARER_PREFIX + token)
                .contentType(JSON_MEDIA_TYPE)
                .accept(JSON_MEDIA_TYPE)
                .content(toJson(body));
    }

    /**
     * Creates a POST request with no body.
     */
    public static MockHttpServletRequestBuilder postJsonNoBody(String url) {
        return post(url)
                .contentType(JSON_MEDIA_TYPE)
                .accept(JSON_MEDIA_TYPE);
    }

    /**
     * Creates a PATCH request with JSON body and Bearer token authentication.
     */
    public static MockHttpServletRequestBuilder patchJsonAuth(String url, Object body, String token) {
        return patch(url)
                .header(AUTH_HEADER, BEARER_PREFIX + token)
                .contentType(JSON_MEDIA_TYPE)
                .accept(JSON_MEDIA_TYPE)
                .content(toJson(body));
    }

    /**
     * Creates a DELETE request with Bearer token authentication.
     */
    public static MockHttpServletRequestBuilder deleteAuth(String url, String token) {
        return delete(url)
                .header(AUTH_HEADER, BEARER_PREFIX + token)
                .accept(JSON_MEDIA_TYPE);
    }

    /**
     * Builds an authorization header map with Bearer token.
     */
    public static Map<String, String> authHeader(String token) {
        Map<String, String> headers = new HashMap<>();
        headers.put(AUTH_HEADER, BEARER_PREFIX + token);

        return headers;
    }

    /**
     * Builds an authorization header pair with Bearer token.
     */
    public static Pair<String, String> authHeaderPair(String token) {
        return Pair.of(AUTH_HEADER, BEARER_PREFIX + token);
    }

    /**
     * Builds a Content-Type header map for JSON.
     */
    public static Map<String, String> jsonContentTypeHeader() {
        Map<String, String> headers = new HashMap<>();
        headers.put(CONTENT_TYPE_HEADER, CONTENT_TYPE_JSON);

        return headers;
    }
}
