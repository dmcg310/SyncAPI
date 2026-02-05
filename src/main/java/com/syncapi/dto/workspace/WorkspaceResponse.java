package com.syncapi.dto.workspace;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Data Transfer Object for workspace response.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WorkspaceResponse {
    private Long id;
    private String name;
    private String description;

    private LocalDateTime createdAt;

    private int memberCount;
    private int folderCount;
    private int environmentCount;

    private List<WorkspaceMemberDTO> members;

    /**
     * Default constructor.
     */
    public WorkspaceResponse() {
    }

    /**
     * Parameterized constructor.
     *
     * @param id          the workspace ID
     * @param name        the workspace name
     * @param description the workspace description
     * @param createdAt   the creation timestamp
     * @param memberCount the number of members
     * @param folderCount the number of folders
     * @param environmentCount the number of environments
     */
    public WorkspaceResponse(Long id, String name, String description, LocalDateTime createdAt,
                             int memberCount, int folderCount, int environmentCount) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.createdAt = createdAt;
        this.memberCount = memberCount;
        this.folderCount = folderCount;
        this.environmentCount = environmentCount;
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

    public int getMemberCount() {
        return memberCount;
    }

    public void setMemberCount(int memberCount) {
        this.memberCount = memberCount;
    }

    public int getFolderCount() {
        return folderCount;
    }

    public void setFolderCount(int folderCount) {
        this.folderCount = folderCount;
    }

    public int getEnvironmentCount() {
        return environmentCount;
    }

    public void setEnvironmentCount(int environmentCount) {
        this.environmentCount = environmentCount;
    }

    public List<WorkspaceMemberDTO> getMembers() {
        return members;
    }

    public void setMembers(List<WorkspaceMemberDTO> members) {
        this.members = members;
    }

    /**
     * DTO for workspace member information.
     */
    public static class WorkspaceMemberDTO {
        private Long userId;
        private String email;
        private String name;

        /**
         * Default constructor.
         */
        public WorkspaceMemberDTO() {
        }

        /**
         * Parameterized constructor.
         *
         * @param userId the user ID
         * @param email  the user's email
         * @param name   the user's name
         */
        public WorkspaceMemberDTO(Long userId, String email, String name) {
            this.userId = userId;
            this.email = email;
            this.name = name;
        }

        public Long getUserId() {
            return userId;
        }

        public void setUserId(Long userId) {
            this.userId = userId;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}
