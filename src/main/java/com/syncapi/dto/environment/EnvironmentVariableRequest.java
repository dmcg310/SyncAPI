package com.syncapi.dto.environment;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO for creating or updating an environment variable.
 */
public class EnvironmentVariableRequest {
    @NotBlank(message = "Key is required")
    private String key;

    @NotBlank(message = "Value is required")
    private String value;

    /**
     * Default constructor.
     */
    public EnvironmentVariableRequest() {
    }

    /**
     * Parameterized constructor.
     *
     * @param key   the variable key
     * @param value the variable value
     */
    public EnvironmentVariableRequest(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
