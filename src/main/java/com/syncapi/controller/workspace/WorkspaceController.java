package com.syncapi.controller.workspace;

import com.syncapi.dto.workspace.AddMemberRequest;
import com.syncapi.dto.workspace.WorkspaceRequest;
import com.syncapi.dto.workspace.WorkspaceResponse;
import com.syncapi.service.workspace.WorkspaceService;
import com.syncapi.util.AuthUtil;
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
@RequestMapping("/api/workspaces")
public class WorkspaceController {
    @Autowired
    private WorkspaceService workspaceService;

    @GetMapping
    public ResponseEntity<List<WorkspaceResponse>> getUserWorkspaces() {
        List<WorkspaceResponse> workspaces = workspaceService.getUserWorkspaces(AuthUtil.getCurrentUserEmail());

        return ResponseEntity.ok(workspaces);
    }

    @GetMapping("/{id}")
    public ResponseEntity<WorkspaceResponse> getWorkspace(@PathVariable Long id) {
        try {
            WorkspaceResponse workspace = workspaceService.getWorkspace(id, AuthUtil.getCurrentUserEmail());

            return ResponseEntity.ok(workspace);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @PostMapping
    public ResponseEntity<WorkspaceResponse> createWorkspace(@Valid @RequestBody WorkspaceRequest request) {
        WorkspaceResponse workspace = workspaceService.createWorkspace(request, AuthUtil.getCurrentUserEmail());

        return ResponseEntity.status(HttpStatus.CREATED).body(workspace);
    }

    // PUT /api/workspaces/{id} - Update workspace
    @PutMapping("/{id}")
    public ResponseEntity<WorkspaceResponse> updateWorkspace(@PathVariable Long id,
                                                             @Valid @RequestBody WorkspaceRequest request) {
        try {
            WorkspaceResponse workspace = workspaceService.updateWorkspace(id, request, AuthUtil.getCurrentUserEmail());

            return ResponseEntity.ok(workspace);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteWorkspace(@PathVariable Long id) {
        try {
            workspaceService.deleteWorkspace(id, AuthUtil.getCurrentUserEmail());

            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @PostMapping("/{id}/members")
    public ResponseEntity<WorkspaceResponse> addMember(@PathVariable Long id,
                                                       @Valid @RequestBody AddMemberRequest request) {
        try {
            WorkspaceResponse workspace = workspaceService.addMember(id, request, AuthUtil.getCurrentUserEmail());

            return ResponseEntity.ok(workspace);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}/members/{userId}")
    public ResponseEntity<WorkspaceResponse> removeMember(@PathVariable Long id, @PathVariable Long userId) {
        try {
            WorkspaceResponse workspace = workspaceService.removeMember(id, userId, AuthUtil.getCurrentUserEmail());

            return ResponseEntity.ok(workspace);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
