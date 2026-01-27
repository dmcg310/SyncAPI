package com.syncapi.controller.execution;

import com.syncapi.dto.execution.ExecutionResponse;
import com.syncapi.service.execution.ExecutionService;
import com.syncapi.util.Util;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for request-execution-related endpoints.
 */
@RestController
@RequestMapping("/api/folders/{folderId}/requests/{requestId}/execute")
public class ExecutionController {
    /**
     * The execution service.
     */
    @Autowired
    private ExecutionService executionService;

    /**
     * Execute a specific request by its Id.
     *
     * @param requestId the request Id
     * @return the execution response
     */
    @PostMapping
    public ResponseEntity<ExecutionResponse> executeRequest(@PathVariable Long requestId) {
        ExecutionResponse response = executionService.execute(requestId, Util.getCurrentUserEmail());

        return ResponseEntity.ok(response);
    }
}
