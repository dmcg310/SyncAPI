package com.syncapi.service.folder;

import com.syncapi.dto.folder.FolderRequest;
import com.syncapi.dto.folder.FolderResponse;
import com.syncapi.entity.Folder;
import com.syncapi.entity.Workspace;
import com.syncapi.repository.folder.FolderRepository;
import com.syncapi.util.Util;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service for folder operations.
 */
@Service
public class FolderService {
    private final FolderRepository folderRepository;
    private final Util util;

    /**
     * Parameterized constructor.
     *
     * @param folderRepository the folder repository
     * @param util             the utility service
     */
    public FolderService(FolderRepository folderRepository, Util util) {
        this.folderRepository = folderRepository;
        this.util = util;
    }

    /**
     * Retrieves all folders for a workspace.
     *
     * @param workspaceId the workspace ID
     * @param email       the user's email
     * @return the list of folder responses
     */
    public List<FolderResponse> getFoldersByWorkspace(Long workspaceId, String email) {
        util.getWorkspaceWithAccessCheck(workspaceId, email);

        List<Folder> folders = folderRepository.findByWorkspaceId(workspaceId);

        return folders.stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * Retrieves a folder by ID.
     *
     * @param folderId the folder ID
     * @param email    the user's email
     * @return the folder response
     */
    public FolderResponse getFolderById(Long folderId, String email) {
        return toResponse(util.getFolderWithAccessCheck(folderId, email));
    }

    /**
     * Creates a new folder.
     *
     * @param workspaceId the workspace ID
     * @param request     the folder request
     * @param email       the user's email
     * @return the folder response
     */
    @Transactional
    public FolderResponse createFolder(Long workspaceId, FolderRequest request, String email) {
        Workspace workspace = util.getWorkspaceWithAccessCheck(workspaceId, email);

        Folder folder = new Folder();
        folder.setName(request.getName());
        folder.setDescription(request.getDescription());
        folder.setWorkspace(workspace);

        return toResponse(folderRepository.save(folder));
    }

    /**
     * Updates a folder.
     *
     * @param folderId the folder ID
     * @param request  the folder request
     * @param email    the user's email
     * @return the folder response
     */
    @Transactional
    public FolderResponse updateFolder(Long folderId, FolderRequest request, String email) {
        Folder folder = util.getFolderWithAccessCheck(folderId, email);
        folder.setName(request.getName());
        folder.setDescription(request.getDescription());

        return toResponse(folderRepository.save(folder));
    }

    /**
     * Partially updates a folder.
     *
     * @param folderId the folder ID
     * @param request  the folder request
     * @param email    the user's email
     * @return the folder response
     */
    @Transactional
    public FolderResponse patchFolder(Long folderId, FolderRequest request, String email) {
        Folder folder = util.getFolderWithAccessCheck(folderId, email);

        if (request.getName() != null) {
            folder.setName(request.getName());
        }
        if (request.getDescription() != null) {
            String description = request.getDescription().isBlank()
                    ? null
                    : request.getDescription();

            folder.setDescription(description);
        }

        return toResponse(folderRepository.save(folder));
    }

    /**
     * Deletes a folder.
     *
     * @param folderId the folder ID
     * @param email    the user's email
     */
    @Transactional
    public void deleteFolder(Long folderId, String email) {
        folderRepository.delete(util.getFolderWithAccessCheck(folderId, email));
    }

    /**
     * Converts a folder entity to a folder response.
     *
     * @param folder the folder entity
     * @return the folder response
     */
    private FolderResponse toResponse(Folder folder) {
        return new FolderResponse(
                folder.getId(),
                folder.getName(),
                folder.getDescription(),
                folder.getCreatedAt(),
                folder.getWorkspace().getId(),
                folder.getRequests().size()
        );
    }
}
