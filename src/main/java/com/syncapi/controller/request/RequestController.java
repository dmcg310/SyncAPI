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

@RestController
@RequestMapping("/api/folders/{folderId}/requests")
public class RequestController {
    @Autowired
    private RequestService requestService;

    @GetMapping
    public ResponseEntity<List<RequestResponse>> getRequestsByFolder(@PathVariable Long folderId) {
        try {
            List<RequestResponse> requests =
                    requestService.getRequestsByFolder(folderId, Util.getCurrentUserEmail());

            return ResponseEntity.ok(requests);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<RequestResponse> getRequest(@PathVariable Long requestId) {
        try {
            RequestResponse request =
                    requestService.getRequestById(requestId, Util.getCurrentUserEmail());

            return ResponseEntity.ok(request);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @PostMapping
    public ResponseEntity<RequestResponse> createRequest(@PathVariable Long folderId,
                                                         @Valid @RequestBody RequestRequest request) {
        try {
            RequestResponse newRequest =
                    requestService.createRequest(folderId, request, Util.getCurrentUserEmail());

            return ResponseEntity.status(HttpStatus.CREATED).body(newRequest);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @PutMapping("/{requestId}")
    public ResponseEntity<RequestResponse> updateRequest(@PathVariable Long requestId,
                                                         @Valid @RequestBody RequestRequest request) {
        try {
            RequestResponse updatedRequest =
                    requestService.updateRequest(requestId, request, Util.getCurrentUserEmail());

            return ResponseEntity.ok(updatedRequest);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @PatchMapping("/{requestId}")
    public ResponseEntity<RequestResponse> patchRequest(@PathVariable Long requestId,
                                                        @RequestBody RequestRequest request) {
        try {
            RequestResponse patchedRequest =
                    requestService.patchRequest(requestId, request, Util.getCurrentUserEmail());

            return ResponseEntity.ok(patchedRequest);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @DeleteMapping("/{requestId}")
    public ResponseEntity<Void> deleteRequest(@PathVariable Long requestId) {
        try {
            requestService.deleteRequest(requestId, Util.getCurrentUserEmail());

            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }
}
