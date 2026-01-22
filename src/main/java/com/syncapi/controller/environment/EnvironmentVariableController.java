package com.syncapi.controller.environment;

import com.syncapi.dto.environment.EnvironmentVariableRequest;
import com.syncapi.dto.environment.EnvironmentVariableResponse;
import com.syncapi.service.environment.EnvironmentVariableService;
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
@RequestMapping("/api/environments/{environmentId}/variables")
public class EnvironmentVariableController {
    @Autowired
    private EnvironmentVariableService environmentVariableService;

    @GetMapping
    public ResponseEntity<List<EnvironmentVariableResponse>> getVariables(@PathVariable Long environmentId) {
        List<EnvironmentVariableResponse> variables = environmentVariableService.getVariablesByEnvironment(
                environmentId, Util.getCurrentUserEmail());

        return ResponseEntity.ok(variables);
    }

    @PostMapping
    public ResponseEntity<EnvironmentVariableResponse> addVariable(
            @PathVariable Long environmentId,
            @Valid @RequestBody EnvironmentVariableRequest request
    ) {
        EnvironmentVariableResponse created = environmentVariableService.addVariable(environmentId, request,
                Util.getCurrentUserEmail());

        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

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

    @DeleteMapping("/{variableId}")
    public ResponseEntity<Void> deleteVariable(@PathVariable Long environmentId,
                                               @PathVariable Long variableId) {
        environmentVariableService.deleteVariable(environmentId, variableId, Util.getCurrentUserEmail());

        return ResponseEntity.noContent().build();
    }
}
