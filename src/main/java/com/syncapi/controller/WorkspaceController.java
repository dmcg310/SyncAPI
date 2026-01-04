package com.syncapi.controller;

import com.syncapi.dto.AddMemberRequest;
import com.syncapi.dto.WorkspaceRequest;
import com.syncapi.dto.WorkspaceResponse;
import com.syncapi.service.WorkspaceService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/workspaces")
public class WorkspaceController {
    @Autowired
    private WorkspaceService workspaceService;

    @GetMapping
    public ResponseEntity<List<WorkspaceResponse>> getUserWorkspaces() {
        List<WorkspaceResponse> workspaces = workspaceService.getUserWorkspaces(getCurrentUserEmail());

        return ResponseEntity.ok(workspaces);
    }

    @GetMapping("/{id}")
    public ResponseEntity<WorkspaceResponse> getWorkspace(@PathVariable Long id) {
        try {
            WorkspaceResponse workspace = workspaceService.getWorkspace(id, getCurrentUserEmail());

            return ResponseEntity.ok(workspace);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @PostMapping
    public ResponseEntity<WorkspaceResponse> createWorkspace(@Valid @RequestBody WorkspaceRequest request) {
        WorkspaceResponse workspace = workspaceService.createWorkspace(request, getCurrentUserEmail());

        return ResponseEntity.status(HttpStatus.CREATED).body(workspace);
    }

    // PUT /api/workspaces/{id} - Update workspace
    @PutMapping("/{id}")
    public ResponseEntity<WorkspaceResponse> updateWorkspace(@PathVariable Long id,
                                                             @Valid @RequestBody WorkspaceRequest request) {
        try {
            WorkspaceResponse workspace = workspaceService.updateWorkspace(id, request, getCurrentUserEmail());

            return ResponseEntity.ok(workspace);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteWorkspace(@PathVariable Long id) {
        try {
            workspaceService.deleteWorkspace(id, getCurrentUserEmail());

            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @PostMapping("/{id}/members")
    public ResponseEntity<WorkspaceResponse> addMember(@PathVariable Long id,
                                                       @Valid @RequestBody AddMemberRequest request) {

        try {
            WorkspaceResponse workspace = workspaceService.addMember(id, request, getCurrentUserEmail());

            return ResponseEntity.ok(workspace);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}/members/{userId}")
    public ResponseEntity<WorkspaceResponse> removeMember(@PathVariable Long id, @PathVariable Long userId) {
        try {
            WorkspaceResponse workspace = workspaceService.removeMember(id, userId, getCurrentUserEmail());

            return ResponseEntity.ok(workspace);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    private String getCurrentUserEmail() {
        Authentication authentication;
        if ((authentication = SecurityContextHolder.getContext().getAuthentication()) == null) {
            throw new RuntimeException("No authenticated user found");
        }

        return authentication.getName();
    }
}
