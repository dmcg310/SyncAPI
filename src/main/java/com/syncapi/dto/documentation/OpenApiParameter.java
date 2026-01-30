package com.syncapi.dto.documentation;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * DTO representing an OpenAPI parameter.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OpenApiParameter {
    private String name;
    private String in; // path, query, header, cookie
    private boolean required;
    private OpenApiSchema schema;

    /**
     * Default constructor.
     */
    public OpenApiParameter() {
    }

    /**
     * Parameterized constructor.
     *
     * @param name     the name of the parameter
     * @param in       the location of the parameter
     * @param required whether the parameter is required
     * @param schema   the schema of the parameter
     */
    public OpenApiParameter(String name, String in, boolean required, OpenApiSchema schema) {
        this.name = name;
        this.in = in;
        this.required = required;
        this.schema = schema;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIn() {
        return in;
    }

    public void setIn(String in) {
        this.in = in;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public OpenApiSchema getSchema() {
        return schema;
    }

    public void setSchema(OpenApiSchema schema) {
        this.schema = schema;
    }
}
