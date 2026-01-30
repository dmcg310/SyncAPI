package com.syncapi.dto.documentation;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Map;

/**
 * DTO representing an OpenAPI response object.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OpenApiResponse {
    private String description;
    private Map<String, OpenApiMediaType> content;

    /**
     * * Default constructor.
     */
    public OpenApiResponse() {
    }

    /**
     * Parameterized constructor.
     *
     * @param description the response description
     * @param content     the response content
     */
    public OpenApiResponse(String description, Map<String, OpenApiMediaType> content) {
        this.description = description;
        this.content = content;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Map<String, OpenApiMediaType> getContent() {
        return content;
    }

    public void setContent(Map<String, OpenApiMediaType> content) {
        this.content = content;
    }
}
