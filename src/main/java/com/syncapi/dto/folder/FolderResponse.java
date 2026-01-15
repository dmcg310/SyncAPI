package com.syncapi.dto.folder;

import java.time.LocalDateTime;

public class FolderResponse {
    private Long id;
    private String name;
    private String description;

    private LocalDateTime createdAt;

    private Long workspaceId;
    private int requestCount;

    public FolderResponse() {
    }

    public FolderResponse(Long id, String name, String description, LocalDateTime createdAt,
                          Long workspaceId, int requestCount) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.createdAt = createdAt;
        this.workspaceId = workspaceId;
        this.requestCount = requestCount;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Long getWorkspaceId() {
        return workspaceId;
    }

    public void setWorkspaceId(Long workspaceId) {
        this.workspaceId = workspaceId;
    }

    public int getRequestCount() {
        return requestCount;
    }

    public void setRequestCount(int requestCount) {
        this.requestCount = requestCount;
    }
}
