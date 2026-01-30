package com.syncapi.dto.documentation;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Map;

/**
 * DTO representing an OpenAPI schema.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OpenApiSchema {
    private String type; // string, integer, object, array, etc.
    private Map<String, OpenApiSchema> properties; // objects
    private OpenApiSchema items; // arrays

    /**
     * Default constructor.
     */
    public OpenApiSchema() {
    }

    /**
     * Convenience constructor for simple types.
     *
     * @param type the type of the schema
     */
    public OpenApiSchema(String type) {
        this.type = type;
    }

    /**
     * Parameterized constructor.
     *
     * @param type       the type of the schema
     * @param properties the properties of the schema
     * @param items      the items of the schema
     */
    public OpenApiSchema(String type, Map<String, OpenApiSchema> properties, OpenApiSchema items) {
        this.type = type;
        this.properties = properties;
        this.items = items;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Map<String, OpenApiSchema> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, OpenApiSchema> properties) {
        this.properties = properties;
    }

    public OpenApiSchema getItems() {
        return items;
    }

    public void setItems(OpenApiSchema items) {
        this.items = items;
    }
}
