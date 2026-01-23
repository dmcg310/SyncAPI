package com.syncapi.controller.environment;

import com.syncapi.dto.environment.EnvironmentRequest;
import com.syncapi.dto.environment.EnvironmentResponse;
import com.syncapi.service.environment.EnvironmentService;
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
 * Controller for environment-related endpoints.
 */
@RestController
@RequestMapping("/api/workspaces/{workspaceId}/environments")
public class EnvironmentController {
    /**
     * The environment service.
     */
    @Autowired
    private EnvironmentService environmentService;

    /**
     * Gets all environments for a workspace.
     *
     * @param workspaceId the workspace Id
     * @return the list of environment responses
     */
    @GetMapping
    public ResponseEntity<List<EnvironmentResponse>> getEnvironmentsByWorkspace(@PathVariable Long workspaceId) {
        List<EnvironmentResponse> environments = environmentService.getEnvironmentsByWorkspace(workspaceId,
                Util.getCurrentUserEmail());

        return ResponseEntity.ok(environments);
    }

    /**
     * Gets an environment by its Id.
     *
     * @param environmentId the environment Id
     * @return the environment response
     */
    @GetMapping("/{environmentId}")
    public ResponseEntity<EnvironmentResponse> getEnvironment(@PathVariable Long environmentId) {
        EnvironmentResponse environment = environmentService.getEnvironmentById(environmentId,
                Util.getCurrentUserEmail());

        return ResponseEntity.ok(environment);
    }

    /**
     * Creates a new environment.
     *
     * @param workspaceId the workspace Id
     * @param request     the environment request
     * @return the created environment response
     */
    @PostMapping
    public ResponseEntity<EnvironmentResponse> createEnvironment(@PathVariable Long workspaceId,
                                                                 @Valid @RequestBody EnvironmentRequest request) {
        EnvironmentResponse environment = environmentService.createEnvironment(workspaceId, request,
                Util.getCurrentUserEmail());

        return ResponseEntity.status(HttpStatus.CREATED).body(environment);
    }

    /**
     * Updates an existing environment.
     *
     * @param environmentId the environment Id
     * @param request       the environment request
     * @return the updated environment response
     */
    @PutMapping("/{environmentId}")
    public ResponseEntity<EnvironmentResponse> updateEnvironment(@PathVariable Long environmentId,
                                                                 @Valid @RequestBody EnvironmentRequest request) {
        EnvironmentResponse environment = environmentService.updateEnvironment(environmentId, request,
                Util.getCurrentUserEmail());

        return ResponseEntity.ok(environment);
    }

    /**
     * Patches an existing environment.
     *
     * @param environmentId the environment Id
     * @param request       the environment request
     * @return the patched environment response
     */
    @PatchMapping("/{environmentId}")
    public ResponseEntity<EnvironmentResponse> patchEnvironment(@PathVariable Long environmentId,
                                                                @RequestBody EnvironmentRequest request) {
        EnvironmentResponse environment = environmentService.patchEnvironment(environmentId, request,
                Util.getCurrentUserEmail());

        return ResponseEntity.ok(environment);
    }

    /**
     * Deletes an environment.
     *
     * @param environmentId the environment Id
     * @return no content response
     */
    @DeleteMapping("/{environmentId}")
    public ResponseEntity<Void> deleteEnvironment(@PathVariable Long environmentId) {
        environmentService.deleteEnvironment(environmentId, Util.getCurrentUserEmail());

        return ResponseEntity.noContent().build();
    }

    /**
     * Deactivates an environment.
     *
     * @param environmentId the environment Id
     * @return the updated environment response
     */
    @PatchMapping("/{environmentId}/activate")
    public ResponseEntity<EnvironmentResponse> activateEnvironment(@PathVariable Long environmentId) {
        EnvironmentResponse environment = environmentService.setEnvironmentActiveStatus(environmentId, true,
                Util.getCurrentUserEmail());

        return ResponseEntity.ok(environment);
    }
}
