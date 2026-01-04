package com.syncapi.dto.workspace;

import jakarta.validation.constraints.NotBlank;

public class WorkspaceRequest {
    @NotBlank(message = "Workspace name is required")
    private String name;

    public WorkspaceRequest() {
    }

    public WorkspaceRequest(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
