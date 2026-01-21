package com.syncapi.dto.environment;

import java.time.LocalDateTime;
import java.util.List;

public class EnvironmentResponse {
    private Long id;
    private String name;
    private String description;
    private boolean isActive;

    private LocalDateTime createdAt;

    private Long workspaceId;
    private int variableCount;

    private List<EnvironmentVariableResponse> variables;

    public EnvironmentResponse() {
    }

    public EnvironmentResponse(Long id, String name, String description, boolean isActive, LocalDateTime createdAt,
                               Long workspaceId, int variableCount) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.isActive = isActive;
        this.createdAt = createdAt;
        this.workspaceId = workspaceId;
        this.variableCount = variableCount;
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

    public boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(boolean active) {
        isActive = active;
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

    public int getVariableCount() {
        return variableCount;
    }

    public void setVariableCount(int variableCount) {
        this.variableCount = variableCount;
    }

    public List<EnvironmentVariableResponse> getVariables() {
        return variables;
    }

    public void setVariables(List<EnvironmentVariableResponse> variables) {
        this.variables = variables;
    }
}
