package com.syncapi.service.workspace;

import com.syncapi.dto.workspace.AddMemberRequest;
import com.syncapi.dto.workspace.WorkspaceRequest;
import com.syncapi.dto.workspace.WorkspaceResponse;
import com.syncapi.entity.User;
import com.syncapi.entity.Workspace;
import com.syncapi.repository.WorkspaceRepository;
import com.syncapi.util.UserUtil;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class WorkspaceService {
    @Autowired
    private WorkspaceRepository workspaceRepository;

    public List<WorkspaceResponse> getUserWorkspaces(String email) {
        User user = UserUtil.getUserByEmail(email);
        List<Workspace> workspaces = workspaceRepository.findByMemberId(user.getId());

        return workspaces.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public WorkspaceResponse getWorkspace(Long workspaceId, String email) {
        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new RuntimeException("Workspace not found or access denied"));

        User user = UserUtil.getUserByEmail(email);
        if (!workspace.getMembers().contains(user)) {
            throw new RuntimeException("Workspace not found or access denied");
        }

        return toDetailedResponse(workspace);
    }

    @Transactional
    public WorkspaceResponse createWorkspace(WorkspaceRequest request, String email) {
        User user = UserUtil.getUserByEmail(email);

        Workspace workspace = new Workspace();
        workspace.setName(request.getName());
        workspace.getMembers().add(user);

        Workspace savedWorkspace = workspaceRepository.save(workspace);

        return toResponse(savedWorkspace);
    }

    @Transactional
    public WorkspaceResponse updateWorkspace(Long workspaceId, WorkspaceRequest request, String email) {
        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new RuntimeException("Workspace not found or access denied"));

        User user = UserUtil.getUserByEmail(email);
        if (!workspace.getMembers().contains(user)) {
            throw new RuntimeException("Workspace not found or access denied");
        }

        workspace.setName(request.getName());
        Workspace updatedWorkspace = workspaceRepository.save(workspace);

        return toResponse(updatedWorkspace);
    }

    @Transactional
    public void deleteWorkspace(Long workspaceId, String email) {
        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new RuntimeException("Workspace not found or access denied"));

        User user = UserUtil.getUserByEmail(email);
        if (!workspace.getMembers().contains(user)) {
            throw new RuntimeException("Workspace not found or access denied");
        }

        workspaceRepository.delete(workspace);
    }

    @Transactional
    public WorkspaceResponse addMember(Long workspaceId, AddMemberRequest request, String email) {
        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new RuntimeException("Workspace not found or access denied"));

        User currentUser = UserUtil.getUserByEmail(email);
        if (!workspace.getMembers().contains(currentUser)) {
            throw new RuntimeException("Workspace not found or access denied");
        }

        User userToAdd = UserUtil.getUserByEmail(request.getEmail());
        if (workspace.getMembers().contains(userToAdd)) {
            throw new RuntimeException("User is already a member of the workspace");
        }

        workspace.getMembers().add(userToAdd);

        Workspace updatedWorkspace = workspaceRepository.save(workspace);

        return toDetailedResponse(updatedWorkspace);
    }

    @Transactional
    public WorkspaceResponse removeMember(Long workspaceId, Long userId, String email) {
        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new RuntimeException("Workspace not found or access denied"));

        User currentUser = UserUtil.getUserByEmail(email);
        if (!workspace.getMembers().contains(currentUser)) {
            throw new RuntimeException("Workspace not found or access denied");
        }

        User userToRemove = UserUtil.getUserById(userId);
        if (!workspace.getMembers().contains(userToRemove)) {
            throw new RuntimeException("User is not a member of the workspace");
        }

        if (workspace.getMembers().size() <= 1) {
            throw new RuntimeException("Cannot remove the last member of the workspace. Delete workspace instead.");
        }

        workspace.getMembers().remove(userToRemove);

        Workspace updatedWorkspace = workspaceRepository.save(workspace);

        return toDetailedResponse(updatedWorkspace);
    }

    private WorkspaceResponse toResponse(Workspace workspace) {
        return new WorkspaceResponse(
                workspace.getId(),
                workspace.getName(),
                workspace.getCreatedAt(),
                workspace.getMembers().size(),
                workspace.getFolders().size()
        );
    }

    private WorkspaceResponse toDetailedResponse(Workspace workspace) {
        List<WorkspaceResponse.WorkspaceMemberDTO> members =
                workspace.getMembers().stream()
                        .map(member -> new WorkspaceResponse.WorkspaceMemberDTO(
                                member.getId(),
                                member.getName(),
                                member.getEmail()
                        ))
                        .toList();

        WorkspaceResponse response = toResponse(workspace);
        response.setMembers(members);

        return response;
    }
}
