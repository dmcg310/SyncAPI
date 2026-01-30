package com.syncapi.dto.documentation;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * DTO representing an OpenAPI Media Type.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OpenApiMediaType {
    private OpenApiSchema schema;
    private Object example;

    /**
     * Default constructor.
     */
    public OpenApiMediaType() {
    }

    /**
     * Parameterized constructor.
     *
     * @param schema  the schema
     * @param example the example
     */
    public OpenApiMediaType(OpenApiSchema schema, Object example) {
        this.schema = schema;
        this.example = example;
    }

    public OpenApiSchema getSchema() {
        return schema;
    }

    public void setSchema(OpenApiSchema schema) {
        this.schema = schema;
    }

    public Object getExample() {
        return example;
    }

    public void setExample(Object example) {
        this.example = example;
    }
}
