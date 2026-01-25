package com.syncapi.dto.auth;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Data Transfer Object for authentication response.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuthResponse {
    private String token;
    private String email;
    private String name;

    /**
     * Default constructor.
     */
    public AuthResponse() {
    }

    /**
     * Parameterized constructor.
     *
     * @param token the authentication token
     * @param email the user's email
     * @param name  the user's name
     */
    public AuthResponse(String token, String email, String name) {
        this.token = token;
        this.email = email;
        this.name = name;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
