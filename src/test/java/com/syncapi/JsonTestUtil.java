package com.syncapi;

import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

public class JsonTestUtil {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public static final String AUTH_HEADER = "Authorization";
    public static final String BEARER_PREFIX = "Bearer ";

    private JsonTestUtil() {
    }

    public static String toJson(Object object) {
        try {
            return OBJECT_MAPPER.writeValueAsString(object);
        } catch (JacksonException e) {
            throw new RuntimeException("Failed to serialize object to JSON", e);
        }
    }

    public static MockHttpServletRequestBuilder postJson(String url, Object body) {
        return post(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(body));
    }

    public static MockHttpServletRequestBuilder getJsonAuth(String url, String token) {
        return get(url)
                .header(AUTH_HEADER, BEARER_PREFIX + token)
                .accept(MediaType.APPLICATION_JSON);
    }

    public static MockHttpServletRequestBuilder postJsonAuth(String url, Object body, String token) {
        return post(url)
                .header(AUTH_HEADER, BEARER_PREFIX + token)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(toJson(body));
    }

    public static MockHttpServletRequestBuilder putJsonAuth(String url, Object body, String token) {
        return put(url)
                .header(AUTH_HEADER, BEARER_PREFIX + token)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(toJson(body));
    }

    public static MockHttpServletRequestBuilder patchJsonAuth(String url, Object body, String token) {
        return patch(url)
                .header(AUTH_HEADER, BEARER_PREFIX + token)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(toJson(body));
    }

    public static MockHttpServletRequestBuilder deleteAuth(String url, String token) {
        return delete(url)
                .header(AUTH_HEADER, BEARER_PREFIX + token)
                .accept(MediaType.APPLICATION_JSON);
    }
}
