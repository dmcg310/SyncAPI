package com.syncapi.dto.documentation;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * DTO representing OpenAPI information.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OpenApiInfo {
    private String title;
    private String description;
    private String version = "1.0.0";

    /**
     * Default constructor.
     */
    public OpenApiInfo() {
    }

    /**
     * Parameterized constructor.
     *
     * @param title       the title
     * @param description the description
     */
    public OpenApiInfo(String title, String description) {
        this.title = title;
        this.description = description;
    }

    /**
     * Parameterized constructor.
     *
     * @param title       the title
     * @param description the description
     * @param version     the version
     */
    public OpenApiInfo(String title, String description, String version) {
        this.title = title;
        this.description = description;
        this.version = version;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }
}
