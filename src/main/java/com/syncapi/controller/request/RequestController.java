package com.syncapi.controller.request;

import com.syncapi.dto.request.RequestRequest;
import com.syncapi.dto.request.RequestResponse;
import com.syncapi.service.request.RequestService;
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
 * Controller for request-related endpoints.
 */
@RestController
@RequestMapping("/api/folders/{folderId}/requests")
public class RequestController {
    /**
     * The request service.
     */
    @Autowired
    private RequestService requestService;

    /**
     * Get all requests in a folder.
     *
     * @param folderId the folder Id
     * @return the list of requests
     */
    @GetMapping
    public ResponseEntity<List<RequestResponse>> getRequestsByFolder(@PathVariable Long folderId) {
        List<RequestResponse> requests =
                requestService.getRequestsByFolder(folderId, Util.getCurrentUserEmail());

        return ResponseEntity.ok(requests);
    }

    /**
     * Get a specific request by its Id.
     *
     * @param requestId the request Id
     * @return the request
     */
    @GetMapping("/{requestId}")
    public ResponseEntity<RequestResponse> getRequest(@PathVariable Long requestId) {
        RequestResponse request = requestService.getRequestById(requestId, Util.getCurrentUserEmail());

        return ResponseEntity.ok(request);
    }

    /**
     * Create a new request in a folder.
     *
     * @param folderId the folder Id
     * @param request  the request data
     * @return the created request
     */
    @PostMapping
    public ResponseEntity<RequestResponse> createRequest(@PathVariable Long folderId,
                                                         @Valid @RequestBody RequestRequest request) {
        RequestResponse newRequest = requestService.createRequest(folderId, request, Util.getCurrentUserEmail());

        return ResponseEntity.status(HttpStatus.CREATED).body(newRequest);
    }

    /**
     * Update an existing request.
     *
     * @param requestId the request Id
     * @param request   the request data
     * @return the updated request
     */
    @PutMapping("/{requestId}")
    public ResponseEntity<RequestResponse> updateRequest(@PathVariable Long requestId,
                                                         @Valid @RequestBody RequestRequest request) {
        RequestResponse updatedRequest = requestService.updateRequest(requestId, request, Util.getCurrentUserEmail());

        return ResponseEntity.ok(updatedRequest);
    }

    /**
     * Patch an existing request.
     *
     * @param requestId the request Id
     * @param request   the request data
     * @return the patched request
     */
    @PatchMapping("/{requestId}")
    public ResponseEntity<RequestResponse> patchRequest(@PathVariable Long requestId,
                                                        @RequestBody RequestRequest request) {
        RequestResponse patchedRequest = requestService.patchRequest(requestId, request, Util.getCurrentUserEmail());

        return ResponseEntity.ok(patchedRequest);
    }

    /**
     * Delete a request.
     *
     * @param requestId the request Id
     * @return no content
     */
    @DeleteMapping("/{requestId}")
    public ResponseEntity<Void> deleteRequest(@PathVariable Long requestId) {
        requestService.deleteRequest(requestId, Util.getCurrentUserEmail());

        return ResponseEntity.noContent().build();
    }

    /**
     * Lock a request.
     *
     * @param requestId the request Id
     * @return the locked request
     */
    @PatchMapping("/{requestId}/lock")
    public ResponseEntity<RequestResponse> lockRequest(@PathVariable Long requestId) {
        RequestResponse lockedRequest = requestService.lockRequest(requestId, Util.getCurrentUserEmail());

        return ResponseEntity.ok(lockedRequest);
    }

    /**
     * Unlock a request.
     *
     * @param requestId the request Id
     * @return the unlocked request
     */
    @PatchMapping("/{requestId}/unlock")
    public ResponseEntity<RequestResponse> unlockRequest(@PathVariable Long requestId) {
        RequestResponse unlockedRequest = requestService.unlockRequest(requestId, Util.getCurrentUserEmail());

        return ResponseEntity.ok(unlockedRequest);
    }
}
