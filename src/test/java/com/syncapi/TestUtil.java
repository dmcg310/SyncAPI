package com.syncapi;

import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

public class TestUtil {
    public static String generateRandomEmail() {
        return "user-" + UUID.randomUUID() + "@example.com";
    }

    public static String generateRandomName() {
        return "name-" + UUID.randomUUID();
    }

    public static String generateRandomPasswordHash() {
        return "hash-" + UUID.randomUUID();
    }

    public static String generateRandomPassword() {
        return "pass-" + UUID.randomUUID();
    }

    public static String toJson(Object object) {
        try {
            return new ObjectMapper().writeValueAsString(object);
        } catch (JacksonException e) {
            throw new RuntimeException("Failed to serialize object to JSON", e);
        }
    }

    public static <T> T fromJson(String json, Class<T> clazz) {
        try {
            return new ObjectMapper().readValue(json, clazz);
        } catch (JacksonException e) {
            throw new RuntimeException("Failed to deserialize JSON to object", e);
        }
    }

    public static MockHttpServletRequestBuilder postJson(String url, Object body) {
        return post(url)
                .characterEncoding("UTF-8")
                .content(toJson(body))
                .contentType(MediaType.APPLICATION_JSON);
    }
}
