package com.syncapi.controller.workspace;

import com.syncapi.dto.workspace.AddMemberRequest;
import com.syncapi.dto.workspace.WorkspaceRequest;
import com.syncapi.dto.workspace.WorkspaceResponse;
import com.syncapi.service.workspace.WorkspaceService;
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
 * Controller for workspace-related endpoints.
 */
@RestController
@RequestMapping("/api/workspaces")
public class WorkspaceController {
    /**
     * The workspace service.
     */
    @Autowired
    private WorkspaceService workspaceService;

    /**
     * Get all workspaces for the current user.
     *
     * @return list of workspaces
     */
    @GetMapping
    public ResponseEntity<List<WorkspaceResponse>> getUserWorkspaces() {
        List<WorkspaceResponse> workspaces = workspaceService.getUserWorkspaces(Util.getCurrentUserEmail());

        return ResponseEntity.ok(workspaces);
    }

    /**
     * Get a specific workspace by Id.
     *
     * @param id the workspace Id
     * @return the workspace
     */
    @GetMapping("/{id}")
    public ResponseEntity<WorkspaceResponse> getWorkspace(@PathVariable Long id) {
        WorkspaceResponse workspace = workspaceService.getWorkspace(id, Util.getCurrentUserEmail());

        return ResponseEntity.ok(workspace);
    }

    /**
     * Create a new workspace.
     *
     * @param request the workspace request
     * @return the created workspace
     */
    @PostMapping
    public ResponseEntity<WorkspaceResponse> createWorkspace(@Valid @RequestBody WorkspaceRequest request) {
        WorkspaceResponse workspace = workspaceService.createWorkspace(request, Util.getCurrentUserEmail());

        return ResponseEntity.status(HttpStatus.CREATED).body(workspace);
    }

    /**
     * Update an existing workspace.
     *
     * @param id      the workspace Id
     * @param request the workspace request
     * @return the updated workspace
     */
    @PutMapping("/{id}")
    public ResponseEntity<WorkspaceResponse> updateWorkspace(@PathVariable Long id,
                                                             @Valid @RequestBody WorkspaceRequest request) {
        WorkspaceResponse workspace = workspaceService.updateWorkspace(id, request, Util.getCurrentUserEmail());

        return ResponseEntity.ok(workspace);
    }

    /**
     * Patch an existing workspace.
     *
     * @param id      the workspace Id
     * @param request the workspace request
     * @return the patched workspace
     */
    @PatchMapping("/{id}")
    public ResponseEntity<WorkspaceResponse> patchWorkspace(@PathVariable Long id,
                                                            @RequestBody WorkspaceRequest request) {
        WorkspaceResponse workspace = workspaceService.patchWorkspace(id, request, Util.getCurrentUserEmail());

        return ResponseEntity.ok(workspace);
    }

    /**
     * Delete a workspace.
     *
     * @param id the workspace Id
     * @return no content response
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteWorkspace(@PathVariable Long id) {
        workspaceService.deleteWorkspace(id, Util.getCurrentUserEmail());

        return ResponseEntity.noContent().build();
    }

    /**
     * Add a member to the workspace.
     *
     * @param id      the workspace Id
     * @param request the add member request
     * @return the updated workspace
     */
    @PostMapping("/{id}/members")
    public ResponseEntity<WorkspaceResponse> addMember(@PathVariable Long id,
                                                       @Valid @RequestBody AddMemberRequest request) {
        WorkspaceResponse workspace = workspaceService.addMember(id, request, Util.getCurrentUserEmail());

        return ResponseEntity.ok(workspace);
    }

    /**
     * Remove a member from the workspace.
     *
     * @param id     the workspace Id
     * @param userId the user Id to remove
     * @return the updated workspace
     */
    @DeleteMapping("/{id}/members/{userId}")
    public ResponseEntity<WorkspaceResponse> removeMember(@PathVariable Long id, @PathVariable Long userId) {
        WorkspaceResponse workspace = workspaceService.removeMember(id, userId, Util.getCurrentUserEmail());

        return ResponseEntity.ok(workspace);
    }
}
