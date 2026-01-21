package com.syncapi.dto.environment;

public class EnvironmentVariableResponse {
    private Long id;
    private String key;
    private String value;
    private Long environmentId;

    public EnvironmentVariableResponse() {
    }

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