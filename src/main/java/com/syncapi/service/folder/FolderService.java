package com.syncapi.service.folder;

import com.syncapi.dto.folder.FolderRequest;
import com.syncapi.dto.folder.FolderResponse;
import com.syncapi.entity.Folder;
import com.syncapi.entity.User;
import com.syncapi.entity.Workspace;
import com.syncapi.repository.FolderRepository;
import com.syncapi.repository.WorkspaceRepository;
import com.syncapi.util.Util;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FolderService {
    private final FolderRepository folderRepository;
    private final WorkspaceRepository workspaceRepository;
    private final Util util;

    public FolderService(FolderRepository folderRepository,
                         WorkspaceRepository workspaceRepository,
                         Util util) {
        this.folderRepository = folderRepository;
        this.workspaceRepository = workspaceRepository;
        this.util = util;
    }

    public List<FolderResponse> getFoldersByWorkspace(Long workspaceId, String email) {
        getWorkspaceWithAccessCheck(workspaceId, email);

        List<Folder> folders = folderRepository.findByWorkspaceId(workspaceId);

        return folders.stream()
                .map(this::toResponse)
                .toList();
    }

    public FolderResponse getFolderById(Long folderId, String email) {
        return toResponse(getFolderWithAccessCheck(folderId, email));
    }

    @Transactional
    public FolderResponse createFolder(Long workspaceId, FolderRequest request, String email) {
        Workspace workspace = getWorkspaceWithAccessCheck(workspaceId, email);

        Folder folder = new Folder();
        folder.setName(request.getName());
        folder.setDescription(request.getDescription());
        folder.setWorkspace(workspace);

        return toResponse(folderRepository.save(folder));
    }

    @Transactional
    public FolderResponse updateFolder(Long folderId, FolderRequest request, String email) {
        Folder folder = getFolderWithAccessCheck(folderId, email);
        folder.setName(request.getName());
        folder.setDescription(request.getDescription());

        return toResponse(folderRepository.save(folder));
    }

    @Transactional
    public FolderResponse patchFolder(Long folderId, FolderRequest request, String email) {
        Folder folder = getFolderWithAccessCheck(folderId, email);

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

    @Transactional
    public void deleteFolder(Long folderId, String email) {
        folderRepository.delete(getFolderWithAccessCheck(folderId, email));
    }

    private Workspace getWorkspaceWithAccessCheck(Long workspaceId, String email) {
        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new RuntimeException("Workspace not found with Id: " + workspaceId));

        User user = util.getUserByEmail(email);
        if (!workspace.getMembers().contains(user)) {
            throw new RuntimeException("Workspace not found or access denied");
        }

        return workspace;
    }

    private Folder getFolderWithAccessCheck(Long folderId, String email) {
        Folder folder = folderRepository.findById(folderId)
                .orElseThrow(() -> new RuntimeException("Folder not found with Id: " + folderId));

        User user = util.getUserByEmail(email);
        if (!folder.getWorkspace().getMembers().contains(user)) {
            throw new RuntimeException("Folder not found or access denied");
        }

        return folder;
    }

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
