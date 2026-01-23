package com.syncapi.service.workspace;

import com.syncapi.dto.workspace.AddMemberRequest;
import com.syncapi.dto.workspace.WorkspaceRequest;
import com.syncapi.dto.workspace.WorkspaceResponse;
import com.syncapi.entity.User;
import com.syncapi.entity.Workspace;
import com.syncapi.exception.BadRequestException;
import com.syncapi.exception.ConflictException;
import com.syncapi.exception.UnauthorizedException;
import com.syncapi.repository.workspace.WorkspaceRepository;
import com.syncapi.util.Util;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for workspace operations.
 */
@Service
public class WorkspaceService {
    private final WorkspaceRepository workspaceRepository;
    private final Util util;

    /**
     * Parameterized constructor.
     *
     * @param workspaceRepository the workspace repository
     * @param util                the utility service
     */
    public WorkspaceService(WorkspaceRepository workspaceRepository, Util util) {
        this.workspaceRepository = workspaceRepository;
        this.util = util;
    }

    /**
     * Retrieves all workspaces for a user.
     *
     * @param email the user's email
     * @return the list of workspace responses
     */
    public List<WorkspaceResponse> getUserWorkspaces(String email) {
        User user = util.getUserByEmail(email);
        List<Workspace> workspaces = workspaceRepository.findByMemberId(user.getId());

        return workspaces.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves a workspace by ID.
     *
     * @param workspaceId the workspace ID
     * @param email       the user's email
     * @return the workspace response
     */
    public WorkspaceResponse getWorkspace(Long workspaceId, String email) {
        return toDetailedResponse(util.getWorkspaceWithAccessCheck(workspaceId, email));
    }

    /**
     * Creates a new workspace.
     *
     * @param request the workspace request
     * @param email   the user's email
     * @return the workspace response
     */
    @Transactional
    public WorkspaceResponse createWorkspace(WorkspaceRequest request, String email) {
        User user = util.getUserByEmail(email);

        Workspace workspace = new Workspace();
        workspace.setName(request.getName());
        workspace.getMembers().add(user);

        Workspace savedWorkspace = workspaceRepository.save(workspace);

        return toResponse(savedWorkspace);
    }

    /**
     * Updates a workspace.
     *
     * @param workspaceId the workspace ID
     * @param request     the workspace request
     * @param email       the user's email
     * @return the workspace response
     */
    @Transactional
    public WorkspaceResponse updateWorkspace(Long workspaceId, WorkspaceRequest request, String email) {
        Workspace workspace = util.getWorkspaceWithAccessCheck(workspaceId, email);
        workspace.setName(request.getName());
        workspace.setDescription(request.getDescription());

        Workspace updatedWorkspace = workspaceRepository.save(workspace);

        return toResponse(updatedWorkspace);
    }

    /**
     * Partially updates a workspace.
     *
     * @param workspaceId the workspace ID
     * @param request     the workspace request
     * @param email       the user's email
     * @return the workspace response
     */
    @Transactional
    public WorkspaceResponse patchWorkspace(Long workspaceId, WorkspaceRequest request, String email) {
        Workspace workspace = util.getWorkspaceWithAccessCheck(workspaceId, email);

        if (request.getName() != null) {
            workspace.setName(request.getName());
        }
        if (request.getDescription() != null) {
            String description = request.getDescription().isBlank()
                    ? null
                    : request.getDescription();

            workspace.setDescription(description);
        }

        return toResponse(workspaceRepository.save(workspace));
    }

    /**
     * Deletes a workspace.
     *
     * @param workspaceId the workspace ID
     * @param email       the user's email
     */
    @Transactional
    public void deleteWorkspace(Long workspaceId, String email) {
        workspaceRepository.delete(util.getWorkspaceWithAccessCheck(workspaceId, email));
    }

    /**
     * Adds a member to a workspace.
     *
     * @param workspaceId the workspace ID
     * @param request     the add member request
     * @param email       the user's email
     * @return the workspace response
     */
    @Transactional
    public WorkspaceResponse addMember(Long workspaceId, AddMemberRequest request, String email) {
        Workspace workspace = util.getWorkspaceWithAccessCheck(workspaceId, email);
        User userToAdd = util.getUserByEmail(request.getEmail());
        if (workspace.getMembers().contains(userToAdd)) {
            throw new ConflictException("User is already a member of the workspace");
        }

        workspace.getMembers().add(userToAdd);

        Workspace updatedWorkspace = workspaceRepository.save(workspace);

        return toDetailedResponse(updatedWorkspace);
    }

    /**
     * Removes a member from a workspace.
     *
     * @param workspaceId the workspace ID
     * @param userId      the user ID to remove
     * @param email       the user's email
     * @return the workspace response
     */
    @Transactional
    public WorkspaceResponse removeMember(Long workspaceId, Long userId, String email) {
        Workspace workspace = util.getWorkspaceWithAccessCheck(workspaceId, email);
        User userToRemove = util.getUserById(userId);
        if (!workspace.getMembers().contains(userToRemove)) {
            throw new UnauthorizedException("User is not a member of the workspace");
        }

        if (workspace.getMembers().size() <= 1) {
            throw new BadRequestException("Cannot remove the last member of the workspace, delete workspace instead");
        }

        workspace.getMembers().remove(userToRemove);

        Workspace updatedWorkspace = workspaceRepository.save(workspace);

        return toDetailedResponse(updatedWorkspace);
    }

    /**
     * Converts a workspace entity to a workspace response.
     *
     * @param workspace the workspace entity
     * @return the workspace response
     */
    private WorkspaceResponse toResponse(Workspace workspace) {
        return new WorkspaceResponse(
                workspace.getId(),
                workspace.getName(),
                workspace.getDescription(),
                workspace.getCreatedAt(),
                workspace.getMembers().size(),
                workspace.getFolders().size()
        );
    }

    /**
     * Converts a workspace entity to a detailed workspace response.
     *
     * @param workspace the workspace entity
     * @return the workspace response
     */
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
