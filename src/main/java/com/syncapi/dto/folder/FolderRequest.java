package com.syncapi.dto.folder;

import jakarta.validation.constraints.NotBlank;

public class FolderRequest {
    @NotBlank(message = "Folder name is required")
    private String name;

    private String description;

    public FolderRequest() {
    }

    public FolderRequest(String name) {
        this.name = name;
    }

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
