package com.syncapi.dto.environment;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Data Transfer Object for environment variable response.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EnvironmentVariableResponse {
    private Long id;
    private String key;
    private String value;
    private Long environmentId;

    /**
     * Default constructor.
     */
    public EnvironmentVariableResponse() {
    }

    /**
     * Parameterized constructor.
     *
     * @param id            the variable ID
     * @param key           the variable key
     * @param value         the variable value
     * @param environmentId the environment ID
     */
    public EnvironmentVariableResponse(Long id, String key, String value, Long environmentId) {
        this.id = id;
        this.key = key;
        this.value = value;
        this.environmentId = environmentId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Long getEnvironmentId() {
        return environmentId;
    }

    public void setEnvironmentId(Long environmentId) {
        this.environmentId = environmentId;
    }
}