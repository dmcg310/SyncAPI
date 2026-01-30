package com.syncapi.dto.documentation;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Map;

/**
 * DTO representing an OpenAPI request body.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OpenApiRequestBody {
    private String description;
    private boolean required;
    private Map<String, OpenApiMediaType> content;

    /**
     * Default constructor.
     */
    public OpenApiRequestBody() {
    }

    /**
     * Parameterized constructor.
     *
     * @param description the description of the request body
     * @param required    indicates if the request body is required
     * @param content     a map of media types to their corresponding OpenApiMediaType objects
     */
    public OpenApiRequestBody(String description, boolean required, Map<String, OpenApiMediaType> content) {
        this.description = description;
        this.required = required;
        this.content = content;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public Map<String, OpenApiMediaType> getContent() {
        return content;
    }

    public void setContent(Map<String, OpenApiMediaType> content) {
        this.content = content;
    }
}
