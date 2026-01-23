package com.syncapi.dto.folder;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO for creating or updating a folder.
 */
public class FolderRequest {
    @NotBlank(message = "Folder name is required")
    private String name;

    private String description;

    /**
     * Default constructor.
     */
    public FolderRequest() {
    }

    /**
     * Parameterized constructor.
     *
     * @param name the folder name
     */
    public FolderRequest(String name) {
        this.name = name;
    }

    /**
     * Parameterized constructor.
     *
     * @param name        the folder name
     * @param description the folder description
     */
    public FolderRequest(String name, String description) {
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
