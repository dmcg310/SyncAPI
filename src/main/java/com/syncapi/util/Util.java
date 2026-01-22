package com.syncapi.util;

import com.syncapi.entity.Environment;
import com.syncapi.entity.EnvironmentVariable;
import com.syncapi.entity.Folder;
import com.syncapi.entity.Request;
import com.syncapi.entity.User;
import com.syncapi.entity.Workspace;
import com.syncapi.exception.AccessDeniedException;
import com.syncapi.exception.ResourceNotFoundException;
import com.syncapi.repository.environment.EnvironmentRepository;
import com.syncapi.repository.environment.EnvironmentVariableRepository;
import com.syncapi.repository.folder.FolderRepository;
import com.syncapi.repository.request.RequestRepository;
import com.syncapi.repository.user.UserRepository;
import com.syncapi.repository.workspace.WorkspaceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class Util {
    private final UserRepository userRepository;
    private final WorkspaceRepository workspaceRepository;
    private final EnvironmentRepository environmentRepository;
    private final EnvironmentVariableRepository environmentVariableRepository;
    private final FolderRepository folderRepository;
    private final RequestRepository requestRepository;

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

    public static String getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResourceNotFoundException("No authenticated user found");
        }

        return authentication.getName();
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
    }

    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));
    }

    public Workspace getWorkspaceWithAccessCheck(Long workspaceId, String email) {
        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Workspace not found: " + workspaceId));

        User user = getUserByEmail(email);
        if (!workspace.getMembers().contains(user)) {
            throw new AccessDeniedException("Access denied to workspace: " + workspaceId);
        }

        return workspace;
    }

    public Environment getEnvironmentWithAccessCheck(Long environmentId, String email) {
        Environment environment = environmentRepository.findById(environmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Environment not found: " + environmentId));

        User user = getUserByEmail(email);
        if (!environment.getWorkspace().getMembers().contains(user)) {
            throw new AccessDeniedException("Access denied to environment: " + environmentId);
        }

        return environment;
    }

    public EnvironmentVariable getEnvironmentVariableWithAccessCheck(Long variableId, String email) {
        EnvironmentVariable variable = environmentVariableRepository.findById(variableId)
                .orElseThrow(() -> new ResourceNotFoundException("Environment variable not found: " + variableId));

        User user = getUserByEmail(email);
        if (!variable.getEnvironment().getWorkspace().getMembers().contains(user)) {
            throw new AccessDeniedException("Access denied to environment variable: " + variableId);
        }

        return variable;
    }

    public Folder getFolderWithAccessCheck(Long folderId, String email) {
        Folder folder = folderRepository.findById(folderId)
                .orElseThrow(() -> new ResourceNotFoundException("Folder not found: " + folderId));

        User user = getUserByEmail(email);
        if (!folder.getWorkspace().getMembers().contains(user)) {
            throw new AccessDeniedException("Access denied to folder: " + folderId);
        }

        return folder;
    }

    public Request getRequestWithAccessCheck(Long requestId, String email) {
        Request request = requestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Request not found: " + requestId));

        User user = getUserByEmail(email);
        if (!request.getFolder().getWorkspace().getMembers().contains(user)) {
            throw new AccessDeniedException("Access denied to request: " + requestId);
        }

        return request;

    }

    public boolean defaultFalse(Boolean value) {
        return Boolean.TRUE.equals(value);
    }
}
