package com.syncapi.dto.request;

import com.syncapi.util.RequestMethod;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Map;

public class RequestRequest {
    @NotBlank(message = "Request name is required")
    private String name;

    private String description;

    @NotNull(message = "Request method is required")
    private RequestMethod method;

    @NotBlank(message = "Request URL is required")
    private String url;

    private Map<String, String> headers;
    private Map<String, Object> body;
    private Map<String, String> authConfig;
    private String authType;

    public RequestRequest() {
    }

    public RequestRequest(String name, RequestMethod method, String url) {
        this.name = name;
        this.method = method;
        this.url = url;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public RequestMethod getMethod() {
        return method;
    }

    public void setMethod(RequestMethod method) {
        this.method = method;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public Map<String, Object> getBody() {
        return body;
    }

    public void setBody(Map<String, Object> body) {
        this.body = body;
    }

    public Map<String, String> getAuthConfig() {
        return authConfig;
    }

    public void setAuthConfig(Map<String, String> authConfig) {
        this.authConfig = authConfig;
    }

    public String getAuthType() {
        return authType;
    }

    public void setAuthType(String authType) {
        this.authType = authType;
    }
}
