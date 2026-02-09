package com.syncapi.dto.documentation;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * DTO representing an OpenAPI server.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OpenApiServer {
    private String url;
    private String description;

    /**
     * Default constructor.
     */
    public OpenApiServer() {
    }

    /**
     * Parameterized constructor.
     *
     * @param url         the server URL
     * @param description the server description
     */
    public OpenApiServer(String url, String description) {
        this.url = url;
        this.description = description;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
