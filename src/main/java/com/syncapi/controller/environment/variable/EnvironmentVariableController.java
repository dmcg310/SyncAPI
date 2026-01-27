package com.syncapi.controller.environment.variable;

import com.syncapi.dto.environment.variable.EnvironmentVariableRequest;
import com.syncapi.dto.environment.variable.EnvironmentVariableResponse;
import com.syncapi.service.environment.variable.EnvironmentVariableService;
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

/**
 * Controller for environment variable-related endpoints.
 */
@RestController
@RequestMapping("/api/environments/{environmentId}/variables")
public class EnvironmentVariableController {
    /**
     * The environment variable service.
     */
    @Autowired
    private EnvironmentVariableService environmentVariableService;

    /**
     * Get all environment variables for a specific environment.
     *
     * @param environmentId the environment Id
     * @return the list of environment variables
     */
    @GetMapping
    public ResponseEntity<List<EnvironmentVariableResponse>> getVariables(@PathVariable Long environmentId) {
        List<EnvironmentVariableResponse> variables = environmentVariableService.getVariablesByEnvironment(
                environmentId, Util.getCurrentUserEmail());

        return ResponseEntity.ok(variables);
    }

    /**
     * Add a new environment variable to a specific environment.
     *
     * @param environmentId the environment Id
     * @param request       the environment variable request
     * @return the created environment variable
     */
    @PostMapping
    public ResponseEntity<EnvironmentVariableResponse> addVariable(
            @PathVariable Long environmentId,
            @Valid @RequestBody EnvironmentVariableRequest request
    ) {
        EnvironmentVariableResponse created = environmentVariableService.addVariable(environmentId, request,
                Util.getCurrentUserEmail());

        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Update an existing environment variable.
     *
     * @param environmentId the environment Id
     * @param variableId    the variable Id
     * @param request       the environment variable request
     * @return the updated environment variable
     */
    @PutMapping("/{variableId}")
    public ResponseEntity<EnvironmentVariableResponse> updateVariable(
            @PathVariable Long environmentId,
            @PathVariable Long variableId,
            @Valid @RequestBody EnvironmentVariableRequest request
    ) {
        EnvironmentVariableResponse updated = environmentVariableService.updateVariable(environmentId, variableId,
                request, Util.getCurrentUserEmail());

        return ResponseEntity.ok(updated);
    }

    /**
     * Delete an environment variable.
     *
     * @param environmentId the environment Id
     * @param variableId    the variable Id
     * @return no content response
     */
    @DeleteMapping("/{variableId}")
    public ResponseEntity<Void> deleteVariable(@PathVariable Long environmentId,
                                               @PathVariable Long variableId) {
        environmentVariableService.deleteVariable(environmentId, variableId, Util.getCurrentUserEmail());

        return ResponseEntity.noContent().build();
    }
}
