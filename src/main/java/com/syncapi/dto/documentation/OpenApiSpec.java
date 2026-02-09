package com.syncapi.dto.documentation;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

/**
 * DTO representing the OpenAPI specification.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OpenApiSpec {
    @JsonProperty("openapi")
    private String openapi = "3.0.0";
    private OpenApiInfo info;
    private Map<String, OpenApiPathItem> paths;

    /**
     * Default constructor.
     */
    public OpenApiSpec() {
    }

    /**
     * Parameterized constructor.
     *
     * @param info  the API information
     * @param paths the API paths
     */
    public OpenApiSpec(OpenApiInfo info, Map<String, OpenApiPathItem> paths) {
        this.info = info;
        this.paths = paths;
    }

    public String getOpenapi() {
        return openapi;
    }

    public void setOpenapi(String openapi) {
        this.openapi = openapi;
    }

    public OpenApiInfo getInfo() {
        return info;
    }

    public void setInfo(OpenApiInfo info) {
        this.info = info;
    }

    public Map<String, OpenApiPathItem> getPaths() {
        return paths;
    }

    public void setPaths(Map<String, OpenApiPathItem> paths) {
        this.paths = paths;
    }
}
