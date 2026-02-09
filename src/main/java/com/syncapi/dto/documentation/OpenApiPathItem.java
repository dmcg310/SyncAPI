package com.syncapi.dto.documentation;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

/**
 * DTO representing an OpenAPI Path Item with various HTTP operations.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OpenApiPathItem {
    private List<OpenApiServer> servers;
    private OpenApiOperation get;
    private OpenApiOperation post;
    private OpenApiOperation put;
    private OpenApiOperation patch;
    private OpenApiOperation delete;

    /**
     * Default constructor.
     */
    public OpenApiPathItem() {
    }

    public OpenApiPathItem(List<OpenApiServer> servers, OpenApiOperation get, OpenApiOperation post,
                           OpenApiOperation put, OpenApiOperation patch, OpenApiOperation delete) {
        this.servers = servers;
        this.get = get;
        this.post = post;
        this.put = put;
        this.patch = patch;
        this.delete = delete;
    }

    public List<OpenApiServer> getServers() {
        return servers;
    }

    public void setServers(List<OpenApiServer> servers) {
        this.servers = servers;
    }

    public OpenApiOperation getGet() {
        return get;
    }

    public void setGet(OpenApiOperation get) {
        this.get = get;
    }

    public OpenApiOperation getPost() {
        return post;
    }

    public void setPost(OpenApiOperation post) {
        this.post = post;
    }

    public OpenApiOperation getPut() {
        return put;
    }

    public void setPut(OpenApiOperation put) {
        this.put = put;
    }

    public OpenApiOperation getPatch() {
        return patch;
    }

    public void setPatch(OpenApiOperation patch) {
        this.patch = patch;
    }

    public OpenApiOperation getDelete() {
        return delete;
    }

    public void setDelete(OpenApiOperation delete) {
        this.delete = delete;
    }
}
