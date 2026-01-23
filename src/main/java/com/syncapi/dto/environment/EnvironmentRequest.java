package com.syncapi.dto.environment;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO for creating or updating an environment.
 */
public class EnvironmentRequest {
    @NotBlank(message = "Environment name is required")
    private String name;

    private String description;
    private Boolean isActive;

    /**
     * Default constructor.
     */
    public EnvironmentRequest() {
    }

    /**
     * Parametrized constructor.
     *
     * @param name the name of the environment
     */
    public EnvironmentRequest(String name) {
        this.name = name;
    }

    /**
     * Parametrized constructor.
     *
     * @param name        the name of the environment
     * @param description the description of the environment
     * @param isActive    the active status of the environment
     */
    public EnvironmentRequest(String name, String description, Boolean isActive) {
        this.name = name;
        this.description = description;
        this.isActive = isActive;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean active) {
        isActive = active;
    }
}
