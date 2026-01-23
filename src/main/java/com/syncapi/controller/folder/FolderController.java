package com.syncapi.controller.folder;

import com.syncapi.dto.folder.FolderRequest;
import com.syncapi.dto.folder.FolderResponse;
import com.syncapi.service.folder.FolderService;
import com.syncapi.util.Util;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controller for folder-related endpoints.
 */
@RestController
@RequestMapping("/api/workspaces/{workspaceId}/folders")
public class FolderController {
    /**
     * The folder service.
     */
    @Autowired
    private FolderService folderService;

    /**
     * Get all folders in a workspace.
     *
     * @param workspaceId the workspace Id
     * @return the list of folders
     */
    @GetMapping
    public ResponseEntity<List<FolderResponse>> getFoldersByWorkspace(@PathVariable Long workspaceId) {
        List<FolderResponse> folders = folderService.getFoldersByWorkspace(workspaceId, Util.getCurrentUserEmail());

        return ResponseEntity.ok(folders);
    }

    /**
     * Get a folder by its Id.
     *
     * @param folderId the folder Id
     * @return the folder
     */
    @GetMapping("/{folderId}")
    public ResponseEntity<FolderResponse> getFolder(@PathVariable Long folderId) {
        FolderResponse folder = folderService.getFolderById(folderId, Util.getCurrentUserEmail());

        return ResponseEntity.ok(folder);
    }

    /**
     * Create a new folder in a workspace.
     *
     * @param workspaceId the workspace Id
     * @param request     the folder request
     * @return the created folder
     */
    @PostMapping
    public ResponseEntity<FolderResponse> createFolder(@PathVariable Long workspaceId,
                                                       @Valid @RequestBody FolderRequest request) {
        FolderResponse folder = folderService.createFolder(workspaceId, request, Util.getCurrentUserEmail());

        return ResponseEntity.status(HttpStatus.CREATED).body(folder);
    }

    /**
     * Update an existing folder.
     *
     * @param folderId the folder Id
     * @param request  the folder request
     * @return the updated folder
     */
    @PutMapping("/{folderId}")
    public ResponseEntity<FolderResponse> updateFolder(@PathVariable Long folderId,
                                                       @Valid @RequestBody FolderRequest request) {
        FolderResponse folder = folderService.updateFolder(folderId, request, Util.getCurrentUserEmail());

        return ResponseEntity.ok(folder);
    }

    /**
     * Patches an existing folder.
     *
     * @param folderId the folder Id
     * @param request  the folder request
     * @return the patched folder
     */
    @PatchMapping("/{folderId}")
    public ResponseEntity<FolderResponse> patchFolder(@PathVariable Long folderId,
                                                      @RequestBody FolderRequest request) {
        FolderResponse folder = folderService.patchFolder(folderId, request, Util.getCurrentUserEmail());

        return ResponseEntity.ok(folder);
    }

    /**
     * Delete a folder by its Id.
     *
     * @param folderId the folder Id
     * @return no content
     */
    @DeleteMapping("/{folderId}")
    public ResponseEntity<Void> deleteFolder(@PathVariable Long folderId) {
        folderService.deleteFolder(folderId, Util.getCurrentUserEmail());

        return ResponseEntity.noContent().build();
    }
}
