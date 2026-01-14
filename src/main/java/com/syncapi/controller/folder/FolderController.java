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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/workspaces/{workspaceId}/folders")
public class FolderController {
    @Autowired
    private FolderService folderService;

    @GetMapping
    public ResponseEntity<List<FolderResponse>> getFoldersByWorkspace(@PathVariable Long workspaceId) {
        try {
            List<FolderResponse> folders = folderService.getFoldersByWorkspace(workspaceId, Util.getCurrentUserEmail());

            return ResponseEntity.ok(folders);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @GetMapping("/{folderId}")
    public ResponseEntity<FolderResponse> getFolder(@PathVariable Long folderId) {
        try {
            FolderResponse folder = folderService.getFolderById(folderId, Util.getCurrentUserEmail());

            return ResponseEntity.ok(folder);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @PostMapping
    public ResponseEntity<FolderResponse> createFolder(@PathVariable Long workspaceId,
                                                       @Valid @RequestBody FolderRequest request) {
        try {
            FolderResponse folder = folderService.createFolder(workspaceId, request, Util.getCurrentUserEmail());

            return ResponseEntity.status(HttpStatus.CREATED).body(folder);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @PutMapping("/{folderId}")
    public ResponseEntity<FolderResponse> updateFolder(@PathVariable Long folderId,
                                                       @Valid @RequestBody FolderRequest request) {
        try {
            FolderResponse folder = folderService.updateFolder(folderId, request, Util.getCurrentUserEmail());

            return ResponseEntity.ok(folder);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @DeleteMapping("/{folderId}")
    public ResponseEntity<Void> deleteFolder(@PathVariable Long folderId) {
        try {
            folderService.deleteFolder(folderId, Util.getCurrentUserEmail());

            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }
}
