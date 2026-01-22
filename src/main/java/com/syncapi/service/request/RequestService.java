package com.syncapi.service.request;

import com.syncapi.dto.request.RequestRequest;
import com.syncapi.dto.request.RequestResponse;
import com.syncapi.entity.Folder;
import com.syncapi.entity.Request;
import com.syncapi.exception.ConflictException;
import com.syncapi.repository.request.RequestRepository;
import com.syncapi.util.Util;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class RequestService {
    private final RequestRepository requestRepository;
    private final Util util;

    public RequestService(RequestRepository requestRepository, Util util) {
        this.requestRepository = requestRepository;
        this.util = util;
    }

    public List<RequestResponse> getRequestsByFolder(Long folderId, String email) {
        util.getFolderWithAccessCheck(folderId, email);
        List<Request> requests = requestRepository.findByFolderId(folderId);

        return requests.stream()
                .map(this::toResponse)
                .toList();
    }

    public RequestResponse getRequestById(Long requestId, String email) {
        return toResponse(util.getRequestWithAccessCheck(requestId, email));
    }

    @Transactional
    public RequestResponse createRequest(Long folderId, RequestRequest request, String email) {
        Folder folder = util.getFolderWithAccessCheck(folderId, email);

        Request newRequest = new Request();
        newRequest.setName(request.getName());
        newRequest.setDescription(request.getDescription());
        newRequest.setMethod(request.getMethod());
        newRequest.setUrl(request.getUrl());
        newRequest.setHeaders(request.getHeaders());
        newRequest.setBody(request.getBody());
        newRequest.setAuthType(request.getAuthType());
        newRequest.setAuthConfig(request.getAuthConfig());
        newRequest.setFolder(folder);

        return toResponse(requestRepository.save(newRequest));
    }

    @Transactional
    public RequestResponse updateRequest(Long requestId, RequestRequest request, String email) {
        Request existingRequest = util.getRequestWithAccessCheck(requestId, email);
        existingRequest.setName(request.getName());
        existingRequest.setDescription(request.getDescription());
        existingRequest.setMethod(request.getMethod());
        existingRequest.setUrl(request.getUrl());
        existingRequest.setHeaders(request.getHeaders());
        existingRequest.setBody(request.getBody());
        existingRequest.setAuthType(request.getAuthType());
        existingRequest.setAuthConfig(request.getAuthConfig());

        return toResponse(requestRepository.save(existingRequest));
    }

    @Transactional
    public RequestResponse patchRequest(Long requestId, RequestRequest request, String email) {
        Request existingRequest = util.getRequestWithAccessCheck(requestId, email);

        if (request.getName() != null) {
            existingRequest.setName(request.getName());
        }
        if (request.getDescription() != null) {
            String description = request.getDescription().isBlank()
                    ? null
                    : request.getDescription();

            existingRequest.setDescription(description);
        }
        if (request.getMethod() != null) {
            existingRequest.setMethod(request.getMethod());
        }
        if (request.getUrl() != null) {
            existingRequest.setUrl(request.getUrl());
        }
        if (request.getHeaders() != null) {
            existingRequest.setHeaders(request.getHeaders());
        }
        if (request.getBody() != null) {
            existingRequest.setBody(request.getBody());
        }
        if (request.getAuthType() != null) {
            existingRequest.setAuthType(request.getAuthType());
        }
        if (request.getAuthConfig() != null) {
            existingRequest.setAuthConfig(request.getAuthConfig());
        }

        return toResponse(requestRepository.save(existingRequest));

    }

    @Transactional
    public void deleteRequest(Long requestId, String email) {
        requestRepository.delete(util.getRequestWithAccessCheck(requestId, email));
    }

    @Transactional
    public RequestResponse lockRequest(Long requestId, String email) {
        Long userId = util.getUserByEmail(email).getId();

        Request existingRequest = util.getRequestWithAccessCheck(requestId, email);
        if (existingRequest.getLockedBy() != null && !existingRequest.getLockedBy().equals(userId)) {
            throw new ConflictException("Request is already locked by another user");
        }

        existingRequest.setLockedBy(userId);
        existingRequest.setLockedAt(LocalDateTime.now());

        return toResponse(requestRepository.save(existingRequest));
    }

    @Transactional
    public RequestResponse unlockRequest(Long requestId, String email) {
        Long userId = util.getUserByEmail(email).getId();

        Request existingRequest = util.getRequestWithAccessCheck(requestId, email);
        if (existingRequest.getLockedBy() == null || !existingRequest.getLockedBy().equals(userId)) {
            throw new ConflictException("Request is not locked by the current user or is already unlocked");
        }

        existingRequest.setLockedBy(null);
        existingRequest.setLockedAt(null);

        return toResponse(requestRepository.save(existingRequest));
    }

    private RequestResponse toResponse(Request request) {
        return new RequestResponse(
                request.getId(),
                request.getName(),
                request.getDescription(),
                request.getMethod(),
                request.getUrl(),
                request.getHeaders(),
                request.getBody(),
                request.getAuthType(),
                request.getAuthConfig(),
                request.getLockedBy(),
                request.getLockedAt(),
                request.getCreatedAt(),
                request.getFolder().getId()
        );
    }
}
