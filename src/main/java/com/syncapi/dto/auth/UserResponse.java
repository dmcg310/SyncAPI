package com.syncapi.dto.auth;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;

/**
 * DTO for user profile response.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserResponse {
    private Long id;
    private String email;
    private String name;
    private LocalDateTime createdAt;

    /**
     * Default constructor.
     */
    public UserResponse() {
    }

    /**
     * Parameterized constructor.
     *
     * @param id        the user ID
     * @param email     the user's email
     * @param name      the user's name
     * @param createdAt the creation timestamp
     */
    public UserResponse(Long id, String email, String name, LocalDateTime createdAt) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
