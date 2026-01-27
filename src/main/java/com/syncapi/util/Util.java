package com.syncapi.util;

import com.syncapi.entity.environment.Environment;
import com.syncapi.entity.environment.variable.EnvironmentVariable;
import com.syncapi.entity.folder.Folder;
import com.syncapi.entity.request.Request;
import com.syncapi.entity.user.User;
import com.syncapi.entity.workspace.Workspace;
import com.syncapi.exception.AccessDeniedException;
import com.syncapi.exception.ResourceNotFoundException;
import com.syncapi.repository.environment.EnvironmentRepository;
import com.syncapi.repository.environment.variable.EnvironmentVariableRepository;
import com.syncapi.repository.folder.FolderRepository;
import com.syncapi.repository.request.RequestRepository;
import com.syncapi.repository.user.UserRepository;
import com.syncapi.repository.workspace.WorkspaceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Utility component for common operations.
 */
@Component
public class Util {
    private final UserRepository userRepository;
    private final WorkspaceRepository workspaceRepository;
    private final EnvironmentRepository environmentRepository;
    private final EnvironmentVariableRepository environmentVariableRepository;
    private final FolderRepository folderRepository;
    private final RequestRepository requestRepository;

    /**
     * Parameterized constructor.
     *
     * @param userRepository                the user repository
     * @param workspaceRepository           the workspace repository
     * @param environmentRepository         the environment repository
     * @param environmentVariableRepository the environment variable repository
     * @param folderRepository              the folder repository
     * @param requestRepository             the request repository
     */
    @Autowired
    public Util(UserRepository userRepository, WorkspaceRepository workspaceRepository,
                EnvironmentRepository environmentRepository,
                EnvironmentVariableRepository environmentVariableRepository, FolderRepository folderRepository,
                RequestRepository requestRepository) {
        this.userRepository = userRepository;
        this.workspaceRepository = workspaceRepository;
        this.environmentRepository = environmentRepository;
        this.environmentVariableRepository = environmentVariableRepository;
        this.folderRepository = folderRepository;
        this.requestRepository = requestRepository;
    }

    /**
     * Gets the current authenticated user's email.
     *
     * @return the user's email
     */
    public static String getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResourceNotFoundException("No authenticated user found");
        }

        return authentication.getName();
    }

    /**
     * Gets a user by email.
     *
     * @param email the user's email
     * @return the user entity
     */
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
    }

    /**
     * Gets a user by ID.
     *
     * @param id the user ID
     * @return the user entity
     */
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));
    }

    /**
     * Gets a workspace and verifies the user has access to it.
     *
     * @param workspaceId the workspace ID
     * @param email       the user's email
     * @return the workspace entity
     */
    public Workspace getWorkspaceWithAccessCheck(Long workspaceId, String email) {
        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Workspace not found: " + workspaceId));

        User user = getUserByEmail(email);
        if (!workspace.getMembers().contains(user)) {
            throw new AccessDeniedException("Access denied to workspace: " + workspaceId);
        }

        return workspace;
    }

    /**
     * Gets an environment and verifies the user has access to it.
     *
     * @param environmentId the environment ID
     * @param email         the user's email
     * @return the environment entity
     */
    public Environment getEnvironmentWithAccessCheck(Long environmentId, String email) {
        Environment environment = environmentRepository.findById(environmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Environment not found: " + environmentId));

        User user = getUserByEmail(email);
        if (!environment.getWorkspace().getMembers().contains(user)) {
            throw new AccessDeniedException("Access denied to environment: " + environmentId);
        }

        return environment;
    }

    /**
     * Gets an environment variable and verifies the user has access to it.
     *
     * @param variableId the variable ID
     * @param email      the user's email
     * @return the environment variable entity
     */
    public EnvironmentVariable getEnvironmentVariableWithAccessCheck(Long variableId, String email) {
        EnvironmentVariable variable = environmentVariableRepository.findById(variableId)
                .orElseThrow(() -> new ResourceNotFoundException("Environment variable not found: " + variableId));

        User user = getUserByEmail(email);
        if (!variable.getEnvironment().getWorkspace().getMembers().contains(user)) {
            throw new AccessDeniedException("Access denied to environment variable: " + variableId);
        }

        return variable;
    }

    /**
     * Gets a folder and verifies the user has access to it.
     *
     * @param folderId the folder ID
     * @param email    the user's email
     * @return the folder entity
     */
    public Folder getFolderWithAccessCheck(Long folderId, String email) {
        Folder folder = folderRepository.findById(folderId)
                .orElseThrow(() -> new ResourceNotFoundException("Folder not found: " + folderId));

        User user = getUserByEmail(email);
        if (!folder.getWorkspace().getMembers().contains(user)) {
            throw new AccessDeniedException("Access denied to folder: " + folderId);
        }

        return folder;
    }

    /**
     * Gets a request and verifies the user has access to it.
     *
     * @param requestId the request ID
     * @param email     the user's email
     * @return the request entity
     */
    public Request getRequestWithAccessCheck(Long requestId, String email) {
        Request request = requestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Request not found: " + requestId));

        User user = getUserByEmail(email);
        if (!request.getFolder().getWorkspace().getMembers().contains(user)) {
            throw new AccessDeniedException("Access denied to request: " + requestId);
        }

        return request;

    }

    /**
     * Converts a Boolean to a boolean, defaulting to false if null.
     *
     * @param value the Boolean value
     * @return true if value is TRUE, false otherwise
     */
    public boolean defaultFalse(Boolean value) {
        return Boolean.TRUE.equals(value);
    }
}
