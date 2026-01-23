package com.syncapi.dto.request;

import com.syncapi.util.RequestMethod;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Data Transfer Object for API request response.
 */
public class RequestResponse {
    private Long id;
    private String name;
    private String description;
    private RequestMethod method;
    private String url;
    private Map<String, String> headers;
    private Map<String, Object> body;
    private String authType;
    private Map<String, String> authConfig;
    private Long lockedBy;
    private LocalDateTime lockedAt;
    private LocalDateTime createdAt;
    private Long folderId;

    /**
     * Default constructor.
     */
    public RequestResponse() {
    }

    /**
     * Parameterized constructor.
     *
     * @param id          the request ID
     * @param name        the request name
     * @param description the request description
     * @param method      the HTTP method
     * @param url         the request URL
     * @param headers     the request headers
     * @param body        the request body
     * @param authType    the authentication type
     * @param authConfig  the authentication configuration
     * @param lockedBy    the user ID who locked the request
     * @param lockedAt    the timestamp when the request was locked
     * @param createdAt   the creation timestamp
     * @param folderId    the folder ID
     */
    public RequestResponse(Long id, String name, String description, RequestMethod method, String url,
                           Map<String, String> headers, Map<String, Object> body, String authType,
                           Map<String, String> authConfig, Long lockedBy, LocalDateTime lockedAt,
                           LocalDateTime createdAt, Long folderId) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.method = method;
        this.url = url;
        this.headers = headers;
        this.body = body;
        this.authType = authType;
        this.authConfig = authConfig;
        this.lockedBy = lockedBy;
        this.lockedAt = lockedAt;
        this.createdAt = createdAt;
        this.folderId = folderId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getAuthType() {
        return authType;
    }

    public void setAuthType(String authType) {
        this.authType = authType;
    }

    public Map<String, String> getAuthConfig() {
        return authConfig;
    }

    public void setAuthConfig(Map<String, String> authConfig) {
        this.authConfig = authConfig;
    }

    public Long getLockedBy() {
        return lockedBy;
    }

    public void setLockedBy(Long lockedBy) {
        this.lockedBy = lockedBy;
    }

    public LocalDateTime getLockedAt() {
        return lockedAt;
    }

    public void setLockedAt(LocalDateTime lockedAt) {
        this.lockedAt = lockedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Long getFolderId() {
        return folderId;
    }

    public void setFolderId(Long folderId) {
        this.folderId = folderId;
    }
}
