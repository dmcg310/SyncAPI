package com.syncapi.controller.documentation;

import com.syncapi.dto.documentation.OpenApiSpec;
import com.syncapi.service.documentation.DocumentationService;
import com.syncapi.util.Util;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for documentation-related endpoints.
 */
@RestController
@RequestMapping("/api/workspaces/{workspaceId}/documentation")
public class DocumentationController {
    /**
     * The documentation service.
     */
    @Autowired
    private DocumentationService documentationService;

    /**
     * Get OpenAPI documentation for a workspace.
     *
     * @param workspaceId the workspace Id
     * @return the OpenAPI documentation
     */
    @GetMapping
    public ResponseEntity<OpenApiSpec> getOpenApiDocumentation(@PathVariable Long workspaceId) {
        return ResponseEntity.ok(documentationService.generateSpec(workspaceId, Util.getCurrentUserEmail()));
    }
}
