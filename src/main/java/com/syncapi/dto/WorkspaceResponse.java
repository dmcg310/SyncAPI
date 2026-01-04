package com.syncapi.dto;

import java.time.LocalDateTime;
import java.util.List;

public class WorkspaceResponse {
    private Long id;
    private String name;

    private LocalDateTime createdAt;

    private int memberCount;
    private int folderCount;

    private List<WorkspaceMemberDTO> members;

    public WorkspaceResponse() {
    }

    public WorkspaceResponse(Long id, String name, LocalDateTime createdAt, int memberCount, int folderCount) {
        this.id = id;
        this.name = name;
        this.createdAt = createdAt;
        this.memberCount = memberCount;
        this.folderCount = folderCount;
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

    public List<WorkspaceMemberDTO> getMembers() {
        return members;
    }

    public void setMembers(List<WorkspaceMemberDTO> members) {
        this.members = members;
    }

    public static class WorkspaceMemberDTO {
        private Long userId;
        private String email;
        private String name;

        public WorkspaceMemberDTO() {
        }

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
