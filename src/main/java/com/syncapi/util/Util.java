package com.syncapi.util;

import com.syncapi.entity.Environment;
import com.syncapi.entity.EnvironmentVariable;
import com.syncapi.entity.Folder;
import com.syncapi.entity.Request;
import com.syncapi.entity.User;
import com.syncapi.entity.Workspace;
import com.syncapi.repository.EnvironmentRepository;
import com.syncapi.repository.EnvironmentVariableRepository;
import com.syncapi.repository.FolderRepository;
import com.syncapi.repository.RequestRepository;
import com.syncapi.repository.UserRepository;
import com.syncapi.repository.WorkspaceRepository;
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
            throw new RuntimeException("No authenticated user found");
        }

        return authentication.getName();
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found: " + email));
    }

    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with Id: " + id));
    }

    public boolean defaultFalse(Boolean value) {
        return Boolean.TRUE.equals(value);
    }

    public Workspace getWorkspaceWithAccessCheck(Long workspaceId, String email) {
        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new RuntimeException("Workspace not found with Id: " + workspaceId));

        User user = getUserByEmail(email);
        if (!workspace.getMembers().contains(user)) {
            throw new RuntimeException("Workspace not found or access denied");
        }

        return workspace;
    }

    public Environment getEnvironmentWithAccessCheck(Long environmentId, String email) {
        Environment environment = environmentRepository.findById(environmentId)
                .orElseThrow(() -> new RuntimeException("Environment not found with Id: " + environmentId));

        User user = getUserByEmail(email);
        if (!environment.getWorkspace().getMembers().contains(user)) {
            throw new RuntimeException("Environment not found or access denied");
        }

        return environment;
    }

    public EnvironmentVariable getEnvironmentVariableWithAccessCheck(Long variableId, String email) {
        EnvironmentVariable variable = environmentVariableRepository.findById(variableId)
                .orElseThrow(() -> new RuntimeException("Environment variable not found with Id: " + variableId));

        User user = getUserByEmail(email);
        if (!variable.getEnvironment().getWorkspace().getMembers().contains(user)) {
            throw new RuntimeException("Environment variable not found or access denied");
        }

        return variable;
    }

    public Folder getFolderWithAccessCheck(Long folderId, String email) {
        Folder folder = folderRepository.findById(folderId)
                .orElseThrow(() -> new RuntimeException("Folder not found with Id: " + folderId));

        User user = getUserByEmail(email);
        if (!folder.getWorkspace().getMembers().contains(user)) {
            throw new RuntimeException("Folder not found or access denied");
        }

        return folder;
    }

    public Request getRequestWithAccessCheck(Long requestId, String email) {
        Request request = requestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found with Id: " + requestId));

        User user = getUserByEmail(email);
        if (!request.getFolder().getWorkspace().getMembers().contains(user)) {
            throw new RuntimeException("Request not found or access denied");
        }

        return request;

    }
}
