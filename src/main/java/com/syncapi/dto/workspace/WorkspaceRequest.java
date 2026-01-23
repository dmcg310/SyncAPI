package com.syncapi.dto.workspace;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO for creating or updating a workspace.
 */
public class WorkspaceRequest {
    @NotBlank(message = "Workspace name is required")
    private String name;

    private String description;

    /**
     * Default constructor.
     */
    public WorkspaceRequest() {
    }

    /**
     * Parameterized constructor.
     *
     * @param name the workspace name
     */
    public WorkspaceRequest(String name) {
        this.name = name;
    }

    /**
     * Parameterized constructor.
     *
     * @param name        the workspace name
     * @param description the workspace description
     */
    public WorkspaceRequest(String name, String description) {
        this.name = name;
        this.description = description;
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
}
