package com.syncapi.service.folder;

import com.syncapi.dto.folder.FolderRequest;
import com.syncapi.dto.folder.FolderResponse;
import com.syncapi.entity.Folder;
import com.syncapi.entity.Workspace;
import com.syncapi.repository.FolderRepository;
import com.syncapi.util.Util;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FolderService {
    private final FolderRepository folderRepository;
    private final Util util;

    public FolderService(FolderRepository folderRepository, Util util) {
        this.folderRepository = folderRepository;
        this.util = util;
    }

    public List<FolderResponse> getFoldersByWorkspace(Long workspaceId, String email) {
        util.getWorkspaceWithAccessCheck(workspaceId, email);

        List<Folder> folders = folderRepository.findByWorkspaceId(workspaceId);

        return folders.stream()
                .map(this::toResponse)
                .toList();
    }

    public FolderResponse getFolderById(Long folderId, String email) {
        return toResponse(util.getFolderWithAccessCheck(folderId, email));
    }

    @Transactional
    public FolderResponse createFolder(Long workspaceId, FolderRequest request, String email) {
        Workspace workspace = util.getWorkspaceWithAccessCheck(workspaceId, email);

        Folder folder = new Folder();
        folder.setName(request.getName());
        folder.setDescription(request.getDescription());
        folder.setWorkspace(workspace);

        return toResponse(folderRepository.save(folder));
    }

    @Transactional
    public FolderResponse updateFolder(Long folderId, FolderRequest request, String email) {
        Folder folder = util.getFolderWithAccessCheck(folderId, email);
        folder.setName(request.getName());
        folder.setDescription(request.getDescription());

        return toResponse(folderRepository.save(folder));
    }

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

    @Transactional
    public void deleteFolder(Long folderId, String email) {
        folderRepository.delete(util.getFolderWithAccessCheck(folderId, email));
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
