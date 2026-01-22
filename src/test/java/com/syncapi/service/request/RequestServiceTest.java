package com.syncapi.service.request;

import com.syncapi.JsonTestUtil;
import com.syncapi.TestUtil;
import com.syncapi.dto.request.RequestRequest;
import com.syncapi.dto.request.RequestResponse;
import com.syncapi.entity.Folder;
import com.syncapi.entity.Request;
import com.syncapi.entity.User;
import com.syncapi.entity.Workspace;
import com.syncapi.repository.request.RequestRepository;
import com.syncapi.util.RequestMethod;
import com.syncapi.util.Util;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.util.ReflectionTestUtils.setField;

@ExtendWith(MockitoExtension.class)
class RequestServiceTest {
    @Mock
    private RequestRepository requestRepository;

    @Mock
    private Util util;

    private RequestService requestService;

    private String testEmail;
    private Folder testFolder;

    @BeforeEach
    void setUp() {
        requestService = new RequestService(requestRepository, util);

        testEmail = TestUtil.generateRandomEmail();
        User testUser = TestUtil.createUser(TestUtil.generateRandomId(), testEmail, TestUtil.generateRandomName());
        Workspace testWorkspace = TestUtil.createWorkspace(TestUtil.generateRandomId(), TestUtil.generateRandomName(),
                testUser);
        testFolder = TestUtil.createFolder(TestUtil.generateRandomId(), TestUtil.generateRandomName(), testWorkspace);
    }

    @Test
    void shouldGetRequestsByFolder() {
        // given
        Request request1 = TestUtil.createRequest(TestUtil.generateRandomId(), TestUtil.generateRandomName(),
                RequestMethod.GET, TestUtil.generateRandomUrl(), testFolder);
        Request request2 = TestUtil.createRequest(TestUtil.generateRandomId(), TestUtil.generateRandomName(),
                RequestMethod.POST, TestUtil.generateRandomUrl(), testFolder);

        when(util.getFolderWithAccessCheck(testFolder.getId(), testEmail)).thenReturn(testFolder);
        when(requestRepository.findByFolderId(testFolder.getId())).thenReturn(List.of(request1, request2));

        // when
        List<RequestResponse> result = requestService.getRequestsByFolder(testFolder.getId(), testEmail);

        // then
        assertThat(result).hasSize(2);
        assertThat(result.getFirst().getId()).isEqualTo(request1.getId());
        assertThat(result.get(1).getId()).isEqualTo(request2.getId());

        verify(util).getFolderWithAccessCheck(testFolder.getId(), testEmail);
        verify(requestRepository).findByFolderId(testFolder.getId());
    }

    @Test
    void shouldReturnEmptyListWhenFolderHasNoRequests() {
        // given
        when(util.getFolderWithAccessCheck(testFolder.getId(), testEmail)).thenReturn(testFolder);
        when(requestRepository.findByFolderId(testFolder.getId())).thenReturn(List.of());

        // when
        List<RequestResponse> result = requestService.getRequestsByFolder(testFolder.getId(), testEmail);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void shouldThrowWhenGettingRequestsFromNonExistentFolder() {
        // given
        Long folderId = TestUtil.generateRandomId();
        when(util.getFolderWithAccessCheck(folderId, testEmail))
                .thenThrow(new RuntimeException("Folder not found with Id: " + folderId));

        // when / then
        assertThatThrownBy(() -> requestService.getRequestsByFolder(folderId, testEmail))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Folder not found with Id: " + folderId);

        verifyNoInteractions(requestRepository);
    }

    @Test
    void shouldThrowWhenGettingRequestsByNonMember() {
        // given
        User otherUser = TestUtil.createUser(TestUtil.generateRandomId(), TestUtil.generateRandomEmail(),
                TestUtil.generateRandomName());
        Workspace otherWorkspace = TestUtil.createWorkspace(TestUtil.generateRandomId(), TestUtil.generateRandomName(),
                otherUser);
        Folder otherFolder = TestUtil.createFolder(TestUtil.generateRandomId(), TestUtil.generateRandomName(),
                otherWorkspace);

        when(util.getFolderWithAccessCheck(otherFolder.getId(), testEmail))
                .thenThrow(new RuntimeException("Folder not found or access denied"));

        // when / then
        assertThatThrownBy(() -> requestService.getRequestsByFolder(otherFolder.getId(), testEmail))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Folder not found or access denied");

        verifyNoInteractions(requestRepository);
    }

    @Test
    void shouldGetRequestById() {
        // given
        Request request = TestUtil.createRequest(TestUtil.generateRandomId(), TestUtil.generateRandomName(),
                RequestMethod.GET, TestUtil.generateRandomUrl(), testFolder);
        request.setDescription(TestUtil.generateRandomDescription());
        request.setHeaders(JsonTestUtil.authHeader(TestUtil.generateRandomToken()));

        when(util.getRequestWithAccessCheck(request.getId(), testEmail)).thenReturn(request);

        // when
        RequestResponse result = requestService.getRequestById(request.getId(), testEmail);

        // then
        assertThat(result.getId()).isEqualTo(request.getId());
        assertThat(result.getName()).isEqualTo(request.getName());
        assertThat(result.getDescription()).isEqualTo(request.getDescription());
        assertThat(result.getMethod()).isEqualTo(request.getMethod());
        assertThat(result.getUrl()).isEqualTo(request.getUrl());
        assertThat(result.getHeaders()).isEqualTo(request.getHeaders());
        assertThat(result.getFolderId()).isEqualTo(testFolder.getId());

        verify(util).getRequestWithAccessCheck(request.getId(), testEmail);
    }

    @Test
    void shouldThrowWhenGettingNonExistentRequest() {
        // given
        Long requestId = TestUtil.generateRandomId();
        when(util.getRequestWithAccessCheck(requestId, testEmail))
                .thenThrow(new RuntimeException("Request not found with Id: " + requestId));

        // when / then
        assertThatThrownBy(() -> requestService.getRequestById(requestId, testEmail))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Request not found with Id: " + requestId);
    }

    @Test
    void shouldThrowWhenGettingRequestByNonMember() {
        // given
        User otherUser = TestUtil.createUser(TestUtil.generateRandomId(), TestUtil.generateRandomEmail(),
                TestUtil.generateRandomName());
        Workspace otherWorkspace = TestUtil.createWorkspace(TestUtil.generateRandomId(), TestUtil.generateRandomName(),
                otherUser);
        Folder otherFolder = TestUtil.createFolder(TestUtil.generateRandomId(), TestUtil.generateRandomName(), otherWorkspace);
        Request request = TestUtil.createRequest(TestUtil.generateRandomId(), TestUtil.generateRandomName(),
                RequestMethod.GET, TestUtil.generateRandomUrl(), otherFolder);

        when(util.getRequestWithAccessCheck(request.getId(), testEmail))
                .thenThrow(new RuntimeException("Request not found or access denied"));

        // when / then
        assertThatThrownBy(() -> requestService.getRequestById(request.getId(), testEmail))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Request not found or access denied");
    }

    @Test
    void shouldCreateRequest() {
        // given
        String requestName = TestUtil.generateRandomName();
        String requestDescription = TestUtil.generateRandomDescription();
        String url = TestUtil.generateRandomUrl();
        Map<String, String> headers = JsonTestUtil.jsonContentTypeHeader();
        Map<String, Object> body = Map.of("name", "John", "age", 30);
        String authType = "bearer";

        RequestRequest request = new RequestRequest(requestName, RequestMethod.POST, url);
        request.setDescription(requestDescription);
        request.setHeaders(headers);
        request.setBody(body);
        request.setAuthType(authType);
        request.setAuthConfig(Map.of("token", "abc123"));

        when(util.getFolderWithAccessCheck(testFolder.getId(), testEmail)).thenReturn(testFolder);
        when(requestRepository.save(any(Request.class))).thenAnswer(invocation -> {
            Request saved = invocation.getArgument(0);
            setField(saved, "id", TestUtil.generateRandomId());
            return saved;
        });

        // when
        RequestResponse result = requestService.createRequest(testFolder.getId(), request, testEmail);

        // then
        assertThat(result.getName()).isEqualTo(requestName);
        assertThat(result.getDescription()).isEqualTo(requestDescription);
        assertThat(result.getMethod()).isEqualTo(RequestMethod.POST);
        assertThat(result.getUrl()).isEqualTo(url);
        assertThat(result.getHeaders()).isEqualTo(headers);
        assertThat(result.getBody()).isEqualTo(body);
        assertThat(result.getAuthType()).isEqualTo(authType);
        assertThat(result.getFolderId()).isEqualTo(testFolder.getId());

        ArgumentCaptor<Request> captor = ArgumentCaptor.forClass(Request.class);
        verify(requestRepository).save(captor.capture());
        assertThat(captor.getValue().getFolder()).isEqualTo(testFolder);
    }

    @Test
    void shouldCreateMinimalRequest() {
        // given
        RequestRequest request = new RequestRequest(TestUtil.generateRandomName(), RequestMethod.GET,
                TestUtil.generateRandomUrl());

        when(util.getFolderWithAccessCheck(testFolder.getId(), testEmail)).thenReturn(testFolder);
        when(requestRepository.save(any(Request.class))).thenAnswer(invocation -> {
            Request saved = invocation.getArgument(0);
            setField(saved, "id", TestUtil.generateRandomId());
            return saved;
        });

        // when
        RequestResponse result = requestService.createRequest(testFolder.getId(), request, testEmail);

        // then
        assertThat(result.getName()).isEqualTo(request.getName());
        assertThat(result.getMethod()).isEqualTo(RequestMethod.GET);
        assertThat(result.getDescription()).isNull();
        assertThat(result.getHeaders()).isNull();
        assertThat(result.getBody()).isNull();
    }

    @Test
    void shouldThrowWhenCreatingRequestInNonExistentFolder() {
        // given
        Long folderId = TestUtil.generateRandomId();
        RequestRequest request = new RequestRequest(TestUtil.generateRandomName(), RequestMethod.GET,
                TestUtil.generateRandomUrl());

        when(util.getFolderWithAccessCheck(folderId, testEmail))
                .thenThrow(new RuntimeException("Folder not found with Id: " + folderId));

        // when / then
        assertThatThrownBy(() -> requestService.createRequest(folderId, request, testEmail))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Folder not found with Id: " + folderId);

        verifyNoInteractions(requestRepository);
    }

    @Test
    void shouldThrowWhenCreatingRequestByNonMember() {
        // given
        User otherUser = TestUtil.createUser(TestUtil.generateRandomId(), TestUtil.generateRandomEmail(),
                TestUtil.generateRandomName());
        Workspace otherWorkspace = TestUtil.createWorkspace(TestUtil.generateRandomId(), TestUtil.generateRandomName(),
                otherUser);
        Folder otherFolder = TestUtil.createFolder(TestUtil.generateRandomId(), TestUtil.generateRandomName(),
                otherWorkspace);

        RequestRequest request = new RequestRequest(TestUtil.generateRandomName(), RequestMethod.GET,
                TestUtil.generateRandomUrl());

        when(util.getFolderWithAccessCheck(otherFolder.getId(), testEmail))
                .thenThrow(new RuntimeException("Folder not found or access denied"));

        // when / then
        assertThatThrownBy(() -> requestService.createRequest(otherFolder.getId(), request, testEmail))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Folder not found or access denied");

        verify(requestRepository, never()).save(any());
    }

    @Test
    void shouldUpdateRequest() {
        // given
        Request existingRequest = TestUtil.createRequest(TestUtil.generateRandomId(), TestUtil.generateRandomName(),
                RequestMethod.GET, TestUtil.generateRandomUrl(), testFolder);
        existingRequest.setDescription(TestUtil.generateRandomDescription());

        String newName = TestUtil.generateRandomName();
        String newUrl = TestUtil.generateRandomUrl();
        String newDescription = TestUtil.generateRandomDescription();
        RequestRequest updateRequest = new RequestRequest(newName, RequestMethod.POST, newUrl);
        updateRequest.setDescription(newDescription);
        updateRequest.setHeaders(Map.of("X-Custom", "value"));

        when(util.getRequestWithAccessCheck(existingRequest.getId(), testEmail)).thenReturn(existingRequest);
        when(requestRepository.save(any(Request.class))).thenReturn(existingRequest);

        // when
        RequestResponse result = requestService.updateRequest(existingRequest.getId(), updateRequest, testEmail);

        // then
        assertThat(result.getName()).isEqualTo(newName);
        assertThat(result.getMethod()).isEqualTo(RequestMethod.POST);
        assertThat(result.getUrl()).isEqualTo(newUrl);
        assertThat(result.getDescription()).isEqualTo(newDescription);
        assertThat(result.getHeaders()).isEqualTo(Map.of("X-Custom", "value"));

        verify(requestRepository).save(existingRequest);
    }

    @Test
    void shouldClearOptionalFieldsOnUpdate() {
        // given
        Request existingRequest = TestUtil.createRequest(TestUtil.generateRandomId(), TestUtil.generateRandomName(),
                RequestMethod.POST, TestUtil.generateRandomUrl(), testFolder);
        existingRequest.setDescription(TestUtil.generateRandomDescription());
        existingRequest.setHeaders(JsonTestUtil.authHeader(TestUtil.generateRandomToken()));
        existingRequest.setBody(Map.of("key", "value"));

        RequestRequest updateRequest = new RequestRequest(TestUtil.generateRandomName(), RequestMethod.GET,
                TestUtil.generateRandomUrl());

        when(util.getRequestWithAccessCheck(existingRequest.getId(), testEmail)).thenReturn(existingRequest);
        when(requestRepository.save(any(Request.class))).thenReturn(existingRequest);

        // when
        RequestResponse result = requestService.updateRequest(existingRequest.getId(), updateRequest, testEmail);

        // then
        assertThat(result.getDescription()).isNull();
        assertThat(result.getHeaders()).isNull();
        assertThat(result.getBody()).isNull();
    }

    @Test
    void shouldThrowWhenUpdatingNonExistentRequest() {
        // given
        Long requestId = TestUtil.generateRandomId();
        RequestRequest request = new RequestRequest(TestUtil.generateRandomName(), RequestMethod.GET,
                TestUtil.generateRandomUrl());

        when(util.getRequestWithAccessCheck(requestId, testEmail))
                .thenThrow(new RuntimeException("Request not found with Id: " + requestId));

        // when / then
        assertThatThrownBy(() -> requestService.updateRequest(requestId, request, testEmail))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Request not found with Id: " + requestId);

        verify(requestRepository, never()).save(any());
    }

    @Test
    void shouldThrowWhenUpdatingRequestByNonMember() {
        // given
        User otherUser = TestUtil.createUser(TestUtil.generateRandomId(), TestUtil.generateRandomEmail(),
                TestUtil.generateRandomName());
        Workspace otherWorkspace = TestUtil.createWorkspace(TestUtil.generateRandomId(), TestUtil.generateRandomName(),
                otherUser);
        Folder otherFolder = TestUtil.createFolder(TestUtil.generateRandomId(), TestUtil.generateRandomName(),
                otherWorkspace);
        Request request = TestUtil.createRequest(TestUtil.generateRandomId(), TestUtil.generateRandomName(),
                RequestMethod.GET, TestUtil.generateRandomUrl(), otherFolder);

        RequestRequest updateRequest = new RequestRequest(TestUtil.generateRandomName(), RequestMethod.POST,
                TestUtil.generateRandomUrl());

        when(util.getRequestWithAccessCheck(request.getId(), testEmail))
                .thenThrow(new RuntimeException("Request not found or access denied"));

        // when / then
        assertThatThrownBy(() -> requestService.updateRequest(request.getId(), updateRequest, testEmail))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Request not found or access denied");

        verify(requestRepository, never()).save(any());
    }

    @Test
    void shouldPatchRequestName() {
        // given
        String existingDescription = TestUtil.generateRandomDescription();
        Request existingRequest = TestUtil.createRequest(TestUtil.generateRandomId(), TestUtil.generateRandomName(),
                RequestMethod.GET, TestUtil.generateRandomUrl(), testFolder);
        existingRequest.setDescription(existingDescription);
        String originalUrl = existingRequest.getUrl();

        String newName = TestUtil.generateRandomName();
        RequestRequest patchRequest = new RequestRequest();
        patchRequest.setName(newName);

        when(util.getRequestWithAccessCheck(existingRequest.getId(), testEmail)).thenReturn(existingRequest);
        when(requestRepository.save(any(Request.class))).thenReturn(existingRequest);

        // when
        RequestResponse result = requestService.patchRequest(existingRequest.getId(), patchRequest, testEmail);

        // then
        assertThat(result.getName()).isEqualTo(newName);
        assertThat(result.getDescription()).isEqualTo(existingDescription);
        assertThat(result.getUrl()).isEqualTo(originalUrl);
        assertThat(result.getMethod()).isEqualTo(RequestMethod.GET);

        verify(requestRepository).save(existingRequest);
    }

    @Test
    void shouldPatchRequestMethod() {
        // given
        Request existingRequest = TestUtil.createRequest(TestUtil.generateRandomId(), TestUtil.generateRandomName(),
                RequestMethod.GET, TestUtil.generateRandomUrl(), testFolder);
        String originalName = existingRequest.getName();

        RequestRequest patchRequest = new RequestRequest();
        patchRequest.setMethod(RequestMethod.POST);

        when(util.getRequestWithAccessCheck(existingRequest.getId(), testEmail)).thenReturn(existingRequest);
        when(requestRepository.save(any(Request.class))).thenReturn(existingRequest);

        // when
        RequestResponse result = requestService.patchRequest(existingRequest.getId(), patchRequest, testEmail);

        // then
        assertThat(result.getMethod()).isEqualTo(RequestMethod.POST);
        assertThat(result.getName()).isEqualTo(originalName);
    }

    @Test
    void shouldPatchRequestUrl() {
        // given
        Request existingRequest = TestUtil.createRequest(TestUtil.generateRandomId(), TestUtil.generateRandomName(),
                RequestMethod.GET, TestUtil.generateRandomUrl(), testFolder);

        String newUrl = TestUtil.generateRandomUrl();
        RequestRequest patchRequest = new RequestRequest();
        patchRequest.setUrl(newUrl);

        when(util.getRequestWithAccessCheck(existingRequest.getId(), testEmail)).thenReturn(existingRequest);
        when(requestRepository.save(any(Request.class))).thenReturn(existingRequest);

        // when
        RequestResponse result = requestService.patchRequest(existingRequest.getId(), patchRequest, testEmail);

        // then
        assertThat(result.getUrl()).isEqualTo(newUrl);
    }

    @Test
    void shouldPatchRequestHeaders() {
        // given
        Request existingRequest = TestUtil.createRequest(TestUtil.generateRandomId(), TestUtil.generateRandomName(),
                RequestMethod.GET, TestUtil.generateRandomUrl(), testFolder);

        Map<String, String> newHeaders = JsonTestUtil.authHeader(TestUtil.generateRandomToken());
        RequestRequest patchRequest = new RequestRequest();
        patchRequest.setHeaders(newHeaders);

        when(util.getRequestWithAccessCheck(existingRequest.getId(), testEmail)).thenReturn(existingRequest);
        when(requestRepository.save(any(Request.class))).thenReturn(existingRequest);

        // when
        RequestResponse result = requestService.patchRequest(existingRequest.getId(), patchRequest, testEmail);

        // then
        assertThat(result.getHeaders()).isEqualTo(newHeaders);
    }

    @Test
    void shouldPatchRequestBody() {
        // given
        Request existingRequest = TestUtil.createRequest(TestUtil.generateRandomId(), TestUtil.generateRandomName(),
                RequestMethod.POST, TestUtil.generateRandomUrl(), testFolder);

        Map<String, Object> newBody = Map.of("userId", 123, "action", "update");
        RequestRequest patchRequest = new RequestRequest();
        patchRequest.setBody(newBody);

        when(util.getRequestWithAccessCheck(existingRequest.getId(), testEmail)).thenReturn(existingRequest);
        when(requestRepository.save(any(Request.class))).thenReturn(existingRequest);

        // when
        RequestResponse result = requestService.patchRequest(existingRequest.getId(), patchRequest, testEmail);

        // then
        assertThat(result.getBody()).isEqualTo(newBody);
    }

    @Test
    void shouldPatchMultipleFields() {
        // given
        Request existingRequest = TestUtil.createRequest(TestUtil.generateRandomId(), TestUtil.generateRandomName(),
                RequestMethod.GET, TestUtil.generateRandomUrl(), testFolder);

        String newName = TestUtil.generateRandomName();
        String newUrl = TestUtil.generateRandomUrl();
        RequestRequest patchRequest = new RequestRequest();
        patchRequest.setName(newName);
        patchRequest.setMethod(RequestMethod.PUT);
        patchRequest.setUrl(newUrl);

        when(util.getRequestWithAccessCheck(existingRequest.getId(), testEmail)).thenReturn(existingRequest);
        when(requestRepository.save(any(Request.class))).thenReturn(existingRequest);

        // when
        RequestResponse result = requestService.patchRequest(existingRequest.getId(), patchRequest, testEmail);

        // then
        assertThat(result.getName()).isEqualTo(newName);
        assertThat(result.getMethod()).isEqualTo(RequestMethod.PUT);
        assertThat(result.getUrl()).isEqualTo(newUrl);
    }

    @Test
    void shouldClearDescriptionWithEmptyString() {
        // given
        Request existingRequest = TestUtil.createRequest(TestUtil.generateRandomId(), TestUtil.generateRandomName(),
                RequestMethod.GET, TestUtil.generateRandomUrl(), testFolder);
        existingRequest.setDescription(TestUtil.generateRandomDescription());

        RequestRequest patchRequest = new RequestRequest();
        patchRequest.setDescription("");

        when(util.getRequestWithAccessCheck(existingRequest.getId(), testEmail)).thenReturn(existingRequest);
        when(requestRepository.save(any(Request.class))).thenReturn(existingRequest);

        // when
        RequestResponse result = requestService.patchRequest(existingRequest.getId(), patchRequest, testEmail);

        // then
        assertThat(result.getDescription()).isNull();
    }

    @Test
    void shouldThrowWhenPatchingNonExistentRequest() {
        // given
        Long requestId = TestUtil.generateRandomId();
        RequestRequest patchRequest = new RequestRequest();
        patchRequest.setName(TestUtil.generateRandomName());

        when(util.getRequestWithAccessCheck(requestId, testEmail))
                .thenThrow(new RuntimeException("Request not found with Id: " + requestId));

        // when / then
        assertThatThrownBy(() -> requestService.patchRequest(requestId, patchRequest, testEmail))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Request not found with Id: " + requestId);

        verify(requestRepository, never()).save(any());
    }

    @Test
    void shouldThrowWhenPatchingRequestByNonMember() {
        // given
        User otherUser = TestUtil.createUser(TestUtil.generateRandomId(), TestUtil.generateRandomEmail(),
                TestUtil.generateRandomName());
        Workspace otherWorkspace = TestUtil.createWorkspace(TestUtil.generateRandomId(), TestUtil.generateRandomName(),
                otherUser);
        Folder otherFolder = TestUtil.createFolder(TestUtil.generateRandomId(), TestUtil.generateRandomName(),
                otherWorkspace);
        Request request = TestUtil.createRequest(TestUtil.generateRandomId(), TestUtil.generateRandomName(),
                RequestMethod.GET, TestUtil.generateRandomUrl(), otherFolder);

        RequestRequest patchRequest = new RequestRequest();
        patchRequest.setName(TestUtil.generateRandomName());

        when(util.getRequestWithAccessCheck(request.getId(), testEmail))
                .thenThrow(new RuntimeException("Request not found or access denied"));

        // when / then
        assertThatThrownBy(() -> requestService.patchRequest(request.getId(), patchRequest, testEmail))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Request not found or access denied");

        verify(requestRepository, never()).save(any());
    }

    @Test
    void shouldDeleteRequest() {
        // given
        Request request = TestUtil.createRequest(TestUtil.generateRandomId(), TestUtil.generateRandomName(),
                RequestMethod.GET, TestUtil.generateRandomUrl(), testFolder);

        when(util.getRequestWithAccessCheck(request.getId(), testEmail)).thenReturn(request);

        // when
        requestService.deleteRequest(request.getId(), testEmail);

        // then
        verify(requestRepository).delete(request);
    }

    @Test
    void shouldThrowWhenDeletingNonExistentRequest() {
        // given
        Long requestId = TestUtil.generateRandomId();

        when(util.getRequestWithAccessCheck(requestId, testEmail))
                .thenThrow(new RuntimeException("Request not found with Id: " + requestId));

        // when / then
        assertThatThrownBy(() -> requestService.deleteRequest(requestId, testEmail))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Request not found with Id: " + requestId);

        verify(requestRepository, never()).delete(any());
    }

    @Test
    void shouldThrowWhenDeletingRequestByNonMember() {
        // given
        User otherUser = TestUtil.createUser(TestUtil.generateRandomId(), TestUtil.generateRandomEmail(),
                TestUtil.generateRandomName());
        Workspace otherWorkspace = TestUtil.createWorkspace(TestUtil.generateRandomId(), TestUtil.generateRandomName(),
                otherUser);
        Folder otherFolder = TestUtil.createFolder(TestUtil.generateRandomId(), TestUtil.generateRandomName(),
                otherWorkspace);
        Request request = TestUtil.createRequest(TestUtil.generateRandomId(), TestUtil.generateRandomName(),
                RequestMethod.GET, TestUtil.generateRandomUrl(), otherFolder);

        when(util.getRequestWithAccessCheck(request.getId(), testEmail))
                .thenThrow(new RuntimeException("Request not found or access denied"));

        // when / then
        assertThatThrownBy(() -> requestService.deleteRequest(request.getId(), testEmail))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Request not found or access denied");

        verify(requestRepository, never()).delete(any());
    }

    @Test
    void shouldLockRequest() {
        // given
        Request request = TestUtil.createRandomRequest(testFolder);
        User user = TestUtil.createUser(TestUtil.generateRandomId(), testEmail, TestUtil.generateRandomName());

        when(util.getUserByEmail(testEmail)).thenReturn(user);
        when(util.getRequestWithAccessCheck(request.getId(), testEmail)).thenReturn(request);
        when(requestRepository.save(any(Request.class))).thenReturn(request);

        // when
        RequestResponse result = requestService.lockRequest(request.getId(), testEmail);

        // then
        assertThat(result.getLockedBy()).isEqualTo(user.getId());
        assertThat(result.getLockedAt()).isNotNull();

        verify(util).getUserByEmail(testEmail);
        verify(util).getRequestWithAccessCheck(request.getId(), testEmail);
        verify(requestRepository).save(request);
    }

    @Test
    void shouldAllowRelockingByTheSameUser() {
        // given
        User user = TestUtil.createUser(TestUtil.generateRandomId(), testEmail, TestUtil.generateRandomName());
        Request request = TestUtil.createRandomRequest(testFolder);
        request.setLockedBy(user.getId());
        request.setLockedAt(LocalDateTime.now().minusHours(1));

        when(util.getUserByEmail(testEmail)).thenReturn(user);
        when(util.getRequestWithAccessCheck(request.getId(), testEmail)).thenReturn(request);
        when(requestRepository.save(any(Request.class))).thenReturn(request);

        // when
        RequestResponse result = requestService.lockRequest(request.getId(), testEmail);

        // then
        assertThat(result.getLockedBy()).isEqualTo(user.getId());

        verify(requestRepository).save(request);
    }

    @Test
    void shouldThrowWhenLockingRequestLockedByAnotherUser() {
        // given
        Long otherUserId = TestUtil.generateRandomId();
        Request request = TestUtil.createRandomRequest(testFolder);
        request.setLockedBy(otherUserId);
        request.setLockedAt(LocalDateTime.now());

        User user = TestUtil.createUser(TestUtil.generateRandomId(), testEmail, TestUtil.generateRandomName());

        when(util.getUserByEmail(testEmail)).thenReturn(user);
        when(util.getRequestWithAccessCheck(request.getId(), testEmail)).thenReturn(request);

        // when / then
        assertThatThrownBy(() -> requestService.lockRequest(request.getId(), testEmail))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("already locked");

        verify(requestRepository, never()).save(any());
    }

    @Test
    void shouldUnlockRequest() {
        // given
        User user = TestUtil.createUser(TestUtil.generateRandomId(), testEmail, TestUtil.generateRandomName());
        Request request = TestUtil.createRandomRequest(testFolder);
        request.setLockedBy(user.getId());
        request.setLockedAt(LocalDateTime.now());

        when(util.getUserByEmail(testEmail)).thenReturn(user);
        when(util.getRequestWithAccessCheck(request.getId(), testEmail)).thenReturn(request);
        when(requestRepository.save(any(Request.class))).thenReturn(request);

        // when
        RequestResponse result = requestService.unlockRequest(request.getId(), testEmail);

        // then
        assertThat(result.getLockedBy()).isNull();
        assertThat(result.getLockedAt()).isNull();

        verify(requestRepository).save(request);
    }

    @Test
    void shouldThrowWhenUnlockingRequestNotLockedByCurrentUser() {
        // given
        Long otherUserId = TestUtil.generateRandomId();
        Request request = TestUtil.createRandomRequest(testFolder);
        request.setLockedBy(otherUserId);
        request.setLockedAt(LocalDateTime.now());

        User user = TestUtil.createUser(TestUtil.generateRandomId(), testEmail, TestUtil.generateRandomName());

        when(util.getUserByEmail(testEmail)).thenReturn(user);
        when(util.getRequestWithAccessCheck(request.getId(), testEmail)).thenReturn(request);

        // when / then
        assertThatThrownBy(() -> requestService.unlockRequest(request.getId(), testEmail))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("not locked by the current user");

        verify(requestRepository, never()).save(any());
    }

    @Test
    void shouldThrowWhenUnlockingAlreadyUnlockedRequest() {
        // given
        Request request = TestUtil.createRandomRequest(testFolder);
        User user = TestUtil.createUser(TestUtil.generateRandomId(), testEmail, TestUtil.generateRandomName());

        when(util.getUserByEmail(testEmail)).thenReturn(user);
        when(util.getRequestWithAccessCheck(request.getId(), testEmail)).thenReturn(request);

        // when / then
        assertThatThrownBy(() -> requestService.unlockRequest(request.getId(), testEmail))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("not locked by the current user");

        verify(requestRepository, never()).save(any());
    }
}
