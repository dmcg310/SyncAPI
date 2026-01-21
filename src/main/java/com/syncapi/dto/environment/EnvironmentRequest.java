package com.syncapi.dto.environment;

import jakarta.validation.constraints.NotBlank;

public class EnvironmentRequest {
    @NotBlank(message = "Environment name is required")
    private String name;

    private String description;
    private Boolean isActive;

    public EnvironmentRequest() {
    }

    public EnvironmentRequest(String name) {
        this.name = name;
    }

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
