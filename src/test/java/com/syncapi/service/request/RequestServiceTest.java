package com.syncapi.service.request;

import com.syncapi.TestUtil;
import com.syncapi.dto.request.RequestRequest;
import com.syncapi.dto.request.RequestResponse;
import com.syncapi.entity.Folder;
import com.syncapi.entity.Request;
import com.syncapi.entity.User;
import com.syncapi.entity.Workspace;
import com.syncapi.repository.FolderRepository;
import com.syncapi.repository.RequestRepository;
import com.syncapi.util.RequestMethod;
import com.syncapi.util.Util;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.util.ReflectionTestUtils.setField;

@ExtendWith(MockitoExtension.class)
class RequestServiceTest {
    private static final String AUTH_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String CONTENT_TYPE_HEADER = "Content-Type";
    private static final String APPLICATION_JSON = "application/json";

    @Mock
    private RequestRepository requestRepository;

    @Mock
    private FolderRepository folderRepository;

    @Mock
    private Util util;

    private RequestService requestService;

    private User testUser;
    private String testEmail;
    private Folder testFolder;

    @BeforeEach
    void setUp() {
        requestService = new RequestService(requestRepository, folderRepository, util);

        testEmail = TestUtil.generateRandomEmail();
        testUser = createUser(TestUtil.generateRandomId(), testEmail, TestUtil.generateRandomName());
        Workspace testWorkspace = createWorkspace(TestUtil.generateRandomId(), TestUtil.generateRandomName(), testUser);
        testFolder = createFolder(TestUtil.generateRandomId(), TestUtil.generateRandomName(), testWorkspace);
    }

    @Test
    void shouldGetRequestsByFolder() {
        // given
        Request request1 = createRequest(TestUtil.generateRandomId(), TestUtil.generateRandomName(),
                RequestMethod.GET, "https://api.example.com/1", testFolder);
        Request request2 = createRequest(TestUtil.generateRandomId(), TestUtil.generateRandomName(),
                RequestMethod.POST, "https://api.example.com/2", testFolder);

        when(folderRepository.findById(testFolder.getId())).thenReturn(Optional.of(testFolder));
        when(util.getUserByEmail(testEmail)).thenReturn(testUser);
        when(requestRepository.findByFolderId(testFolder.getId())).thenReturn(List.of(request1, request2));

        // when
        List<RequestResponse> result = requestService.getRequestsByFolder(testFolder.getId(), testEmail);

        // then
        assertThat(result).hasSize(2);
        assertThat(result.getFirst().getId()).isEqualTo(request1.getId());
        assertThat(result.get(1).getId()).isEqualTo(request2.getId());

        verify(folderRepository).findById(testFolder.getId());
        verify(requestRepository).findByFolderId(testFolder.getId());
    }

    @Test
    void shouldReturnEmptyListWhenFolderHasNoRequests() {
        // given
        when(folderRepository.findById(testFolder.getId())).thenReturn(Optional.of(testFolder));
        when(util.getUserByEmail(testEmail)).thenReturn(testUser);
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
        when(folderRepository.findById(folderId)).thenReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() -> requestService.getRequestsByFolder(folderId, testEmail))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Folder not found with Id: " + folderId);

        verifyNoInteractions(requestRepository);
    }

    @Test
    void shouldThrowWhenGettingRequestsByNonMember() {
        // given
        User otherUser = createUser(TestUtil.generateRandomId(), TestUtil.generateRandomEmail(),
                TestUtil.generateRandomName());
        Workspace otherWorkspace = createWorkspace(TestUtil.generateRandomId(), TestUtil.generateRandomName(),
                otherUser);
        Folder otherFolder = createFolder(TestUtil.generateRandomId(), TestUtil.generateRandomName(), otherWorkspace);

        when(folderRepository.findById(otherFolder.getId())).thenReturn(Optional.of(otherFolder));
        when(util.getUserByEmail(testEmail)).thenReturn(testUser);

        // when / then
        assertThatThrownBy(() -> requestService.getRequestsByFolder(otherFolder.getId(), testEmail))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Folder not found or access denied");

        verifyNoInteractions(requestRepository);
    }

    @Test
    void shouldGetRequestById() {
        // given
        Request request = createRequest(TestUtil.generateRandomId(), TestUtil.generateRandomName(),
                RequestMethod.GET, "https://api.example.com", testFolder);
        request.setDescription(TestUtil.generateRandomValue("description"));
        request.setHeaders(Map.of(AUTH_HEADER, BEARER_PREFIX + "token"));

        when(requestRepository.findById(request.getId())).thenReturn(Optional.of(request));
        when(util.getUserByEmail(testEmail)).thenReturn(testUser);

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

        verify(requestRepository).findById(request.getId());
    }

    @Test
    void shouldThrowWhenGettingNonExistentRequest() {
        // given
        Long requestId = TestUtil.generateRandomId();
        when(requestRepository.findById(requestId)).thenReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() -> requestService.getRequestById(requestId, testEmail))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Request not found with Id: " + requestId);
    }

    @Test
    void shouldThrowWhenGettingRequestByNonMember() {
        // given
        User otherUser = createUser(TestUtil.generateRandomId(), TestUtil.generateRandomEmail(),
                TestUtil.generateRandomName());
        Workspace otherWorkspace = createWorkspace(TestUtil.generateRandomId(), TestUtil.generateRandomName(),
                otherUser);
        Folder otherFolder = createFolder(TestUtil.generateRandomId(), TestUtil.generateRandomName(), otherWorkspace);
        Request request = createRequest(TestUtil.generateRandomId(), TestUtil.generateRandomName(),
                RequestMethod.GET, "https://api.example.com", otherFolder);

        when(requestRepository.findById(request.getId())).thenReturn(Optional.of(request));
        when(util.getUserByEmail(testEmail)).thenReturn(testUser);

        // when / then
        assertThatThrownBy(() -> requestService.getRequestById(request.getId(), testEmail))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Request not found or access denied");
    }

    @Test
    void shouldCreateRequest() {
        // given
        String requestName = TestUtil.generateRandomName();
        String requestDescription = TestUtil.generateRandomValue("description");
        String url = "https://api.example.com/users";
        Map<String, String> headers = Map.of(CONTENT_TYPE_HEADER, APPLICATION_JSON);
        Map<String, Object> body = Map.of("name", "John", "age", 30);
        String authType = "bearer";

        RequestRequest request = new RequestRequest(requestName, RequestMethod.POST, url);
        request.setDescription(requestDescription);
        request.setHeaders(headers);
        request.setBody(body);
        request.setAuthType(authType);
        request.setAuthConfig(Map.of("token", "abc123"));

        when(folderRepository.findById(testFolder.getId())).thenReturn(Optional.of(testFolder));
        when(util.getUserByEmail(testEmail)).thenReturn(testUser);
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
                "https://api.example.com");

        when(folderRepository.findById(testFolder.getId())).thenReturn(Optional.of(testFolder));
        when(util.getUserByEmail(testEmail)).thenReturn(testUser);
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
                "https://api.example.com");

        when(folderRepository.findById(folderId)).thenReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() -> requestService.createRequest(folderId, request, testEmail))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Folder not found with Id: " + folderId);

        verifyNoInteractions(requestRepository);
    }

    @Test
    void shouldThrowWhenCreatingRequestByNonMember() {
        // given
        User otherUser = createUser(TestUtil.generateRandomId(), TestUtil.generateRandomEmail(),
                TestUtil.generateRandomName());
        Workspace otherWorkspace = createWorkspace(TestUtil.generateRandomId(), TestUtil.generateRandomName(),
                otherUser);
        Folder otherFolder = createFolder(TestUtil.generateRandomId(), TestUtil.generateRandomName(), otherWorkspace);

        RequestRequest request = new RequestRequest(TestUtil.generateRandomName(), RequestMethod.GET,
                "https://api.example.com");

        when(folderRepository.findById(otherFolder.getId())).thenReturn(Optional.of(otherFolder));
        when(util.getUserByEmail(testEmail)).thenReturn(testUser);

        // when / then
        assertThatThrownBy(() -> requestService.createRequest(otherFolder.getId(), request, testEmail))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Folder not found or access denied");

        verify(requestRepository, never()).save(any());
    }

    @Test
    void shouldUpdateRequest() {
        // given
        Request existingRequest = createRequest(TestUtil.generateRandomId(), TestUtil.generateRandomName(),
                RequestMethod.GET, "https://old.example.com", testFolder);
        existingRequest.setDescription(TestUtil.generateRandomValue("description"));

        String newName = TestUtil.generateRandomName();
        String newUrl = "https://new.example.com";
        String newDescription = TestUtil.generateRandomValue("description");
        RequestRequest updateRequest = new RequestRequest(newName, RequestMethod.POST, newUrl);
        updateRequest.setDescription(newDescription);
        updateRequest.setHeaders(Map.of("X-Custom", "value"));

        when(requestRepository.findById(existingRequest.getId())).thenReturn(Optional.of(existingRequest));
        when(util.getUserByEmail(testEmail)).thenReturn(testUser);
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
        Request existingRequest = createRequest(TestUtil.generateRandomId(), TestUtil.generateRandomName(),
                RequestMethod.POST, "https://api.example.com", testFolder);
        existingRequest.setDescription(TestUtil.generateRandomValue("description"));
        existingRequest.setHeaders(Map.of(AUTH_HEADER, BEARER_PREFIX + "token"));
        existingRequest.setBody(Map.of("key", "value"));

        RequestRequest updateRequest = new RequestRequest(TestUtil.generateRandomName(), RequestMethod.GET,
                "https://api.example.com");
        // Not setting description, headers, body - should clear them

        when(requestRepository.findById(existingRequest.getId())).thenReturn(Optional.of(existingRequest));
        when(util.getUserByEmail(testEmail)).thenReturn(testUser);
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
                "https://api.example.com");

        when(requestRepository.findById(requestId)).thenReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() -> requestService.updateRequest(requestId, request, testEmail))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Request not found with Id: " + requestId);

        verify(requestRepository, never()).save(any());
    }

    @Test
    void shouldThrowWhenUpdatingRequestByNonMember() {
        // given
        User otherUser = createUser(TestUtil.generateRandomId(), TestUtil.generateRandomEmail(),
                TestUtil.generateRandomName());
        Workspace otherWorkspace = createWorkspace(TestUtil.generateRandomId(), TestUtil.generateRandomName(),
                otherUser);
        Folder otherFolder = createFolder(TestUtil.generateRandomId(), TestUtil.generateRandomName(), otherWorkspace);
        Request request = createRequest(TestUtil.generateRandomId(), TestUtil.generateRandomName(),
                RequestMethod.GET, "https://api.example.com", otherFolder);

        RequestRequest updateRequest = new RequestRequest(TestUtil.generateRandomName(), RequestMethod.POST,
                "https://new.example.com");

        when(requestRepository.findById(request.getId())).thenReturn(Optional.of(request));
        when(util.getUserByEmail(testEmail)).thenReturn(testUser);

        // when / then
        assertThatThrownBy(() -> requestService.updateRequest(request.getId(), updateRequest, testEmail))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Request not found or access denied");

        verify(requestRepository, never()).save(any());
    }

    @Test
    void shouldPatchRequestName() {
        // given
        String existingDescription = TestUtil.generateRandomValue("description");
        Request existingRequest = createRequest(TestUtil.generateRandomId(), TestUtil.generateRandomName(),
                RequestMethod.GET, "https://api.example.com", testFolder);
        existingRequest.setDescription(existingDescription);
        String originalUrl = existingRequest.getUrl();

        String newName = TestUtil.generateRandomName();
        RequestRequest patchRequest = new RequestRequest();
        patchRequest.setName(newName);

        when(requestRepository.findById(existingRequest.getId())).thenReturn(Optional.of(existingRequest));
        when(util.getUserByEmail(testEmail)).thenReturn(testUser);
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
        Request existingRequest = createRequest(TestUtil.generateRandomId(), TestUtil.generateRandomName(),
                RequestMethod.GET, "https://api.example.com", testFolder);
        String originalName = existingRequest.getName();

        RequestRequest patchRequest = new RequestRequest();
        patchRequest.setMethod(RequestMethod.POST);

        when(requestRepository.findById(existingRequest.getId())).thenReturn(Optional.of(existingRequest));
        when(util.getUserByEmail(testEmail)).thenReturn(testUser);
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
        Request existingRequest = createRequest(TestUtil.generateRandomId(), TestUtil.generateRandomName(),
                RequestMethod.GET, "https://old.example.com", testFolder);

        String newUrl = "https://new.example.com";
        RequestRequest patchRequest = new RequestRequest();
        patchRequest.setUrl(newUrl);

        when(requestRepository.findById(existingRequest.getId())).thenReturn(Optional.of(existingRequest));
        when(util.getUserByEmail(testEmail)).thenReturn(testUser);
        when(requestRepository.save(any(Request.class))).thenReturn(existingRequest);

        // when
        RequestResponse result = requestService.patchRequest(existingRequest.getId(), patchRequest, testEmail);

        // then
        assertThat(result.getUrl()).isEqualTo(newUrl);
    }

    @Test
    void shouldPatchRequestHeaders() {
        // given
        Request existingRequest = createRequest(TestUtil.generateRandomId(), TestUtil.generateRandomName(),
                RequestMethod.GET, "https://api.example.com", testFolder);

        Map<String, String> newHeaders = Map.of(AUTH_HEADER, BEARER_PREFIX + "newtoken");
        RequestRequest patchRequest = new RequestRequest();
        patchRequest.setHeaders(newHeaders);

        when(requestRepository.findById(existingRequest.getId())).thenReturn(Optional.of(existingRequest));
        when(util.getUserByEmail(testEmail)).thenReturn(testUser);
        when(requestRepository.save(any(Request.class))).thenReturn(existingRequest);

        // when
        RequestResponse result = requestService.patchRequest(existingRequest.getId(), patchRequest, testEmail);

        // then
        assertThat(result.getHeaders()).isEqualTo(newHeaders);
    }

    @Test
    void shouldPatchRequestBody() {
        // given
        Request existingRequest = createRequest(TestUtil.generateRandomId(), TestUtil.generateRandomName(),
                RequestMethod.POST, "https://api.example.com", testFolder);

        Map<String, Object> newBody = Map.of("userId", 123, "action", "update");
        RequestRequest patchRequest = new RequestRequest();
        patchRequest.setBody(newBody);

        when(requestRepository.findById(existingRequest.getId())).thenReturn(Optional.of(existingRequest));
        when(util.getUserByEmail(testEmail)).thenReturn(testUser);
        when(requestRepository.save(any(Request.class))).thenReturn(existingRequest);

        // when
        RequestResponse result = requestService.patchRequest(existingRequest.getId(), patchRequest, testEmail);

        // then
        assertThat(result.getBody()).isEqualTo(newBody);
    }

    @Test
    void shouldPatchMultipleFields() {
        // given
        Request existingRequest = createRequest(TestUtil.generateRandomId(), TestUtil.generateRandomName(),
                RequestMethod.GET, "https://old.example.com", testFolder);

        String newName = TestUtil.generateRandomName();
        String newUrl = "https://new.example.com";
        RequestRequest patchRequest = new RequestRequest();
        patchRequest.setName(newName);
        patchRequest.setMethod(RequestMethod.PUT);
        patchRequest.setUrl(newUrl);

        when(requestRepository.findById(existingRequest.getId())).thenReturn(Optional.of(existingRequest));
        when(util.getUserByEmail(testEmail)).thenReturn(testUser);
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
        Request existingRequest = createRequest(TestUtil.generateRandomId(), TestUtil.generateRandomName(),
                RequestMethod.GET, "https://api.example.com", testFolder);
        existingRequest.setDescription("Original description");

        RequestRequest patchRequest = new RequestRequest();
        patchRequest.setDescription("");

        when(requestRepository.findById(existingRequest.getId())).thenReturn(Optional.of(existingRequest));
        when(util.getUserByEmail(testEmail)).thenReturn(testUser);
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

        when(requestRepository.findById(requestId)).thenReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() -> requestService.patchRequest(requestId, patchRequest, testEmail))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Request not found with Id: " + requestId);

        verify(requestRepository, never()).save(any());
    }

    @Test
    void shouldThrowWhenPatchingRequestByNonMember() {
        // given
        User otherUser = createUser(TestUtil.generateRandomId(), TestUtil.generateRandomEmail(),
                TestUtil.generateRandomName());
        Workspace otherWorkspace = createWorkspace(TestUtil.generateRandomId(), TestUtil.generateRandomName(),
                otherUser);
        Folder otherFolder = createFolder(TestUtil.generateRandomId(), TestUtil.generateRandomName(), otherWorkspace);
        Request request = createRequest(TestUtil.generateRandomId(), TestUtil.generateRandomName(),
                RequestMethod.GET, "https://api.example.com", otherFolder);

        RequestRequest patchRequest = new RequestRequest();
        patchRequest.setName(TestUtil.generateRandomName());

        when(requestRepository.findById(request.getId())).thenReturn(Optional.of(request));
        when(util.getUserByEmail(testEmail)).thenReturn(testUser);

        // when / then
        assertThatThrownBy(() -> requestService.patchRequest(request.getId(), patchRequest, testEmail))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Request not found or access denied");

        verify(requestRepository, never()).save(any());
    }

    @Test
    void shouldDeleteRequest() {
        // given
        Request request = createRequest(TestUtil.generateRandomId(), TestUtil.generateRandomName(),
                RequestMethod.GET, "https://api.example.com", testFolder);

        when(requestRepository.findById(request.getId())).thenReturn(Optional.of(request));
        when(util.getUserByEmail(testEmail)).thenReturn(testUser);

        // when
        requestService.deleteRequest(request.getId(), testEmail);

        // then
        verify(requestRepository).delete(request);
    }

    @Test
    void shouldThrowWhenDeletingNonExistentRequest() {
        // given
        Long requestId = TestUtil.generateRandomId();

        when(requestRepository.findById(requestId)).thenReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() -> requestService.deleteRequest(requestId, testEmail))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Request not found with Id: " + requestId);

        verify(requestRepository, never()).delete(any());
    }

    @Test
    void shouldThrowWhenDeletingRequestByNonMember() {
        // given
        User otherUser = createUser(TestUtil.generateRandomId(), TestUtil.generateRandomEmail(),
                TestUtil.generateRandomName());
        Workspace otherWorkspace = createWorkspace(TestUtil.generateRandomId(), TestUtil.generateRandomName(),
                otherUser);
        Folder otherFolder = createFolder(TestUtil.generateRandomId(), TestUtil.generateRandomName(), otherWorkspace);
        Request request = createRequest(TestUtil.generateRandomId(), TestUtil.generateRandomName(),
                RequestMethod.GET, "https://api.example.com", otherFolder);

        when(requestRepository.findById(request.getId())).thenReturn(Optional.of(request));
        when(util.getUserByEmail(testEmail)).thenReturn(testUser);

        // when / then
        assertThatThrownBy(() -> requestService.deleteRequest(request.getId(), testEmail))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Request not found or access denied");

        verify(requestRepository, never()).delete(any());
    }

    private User createUser(Long id, String email, String name) {
        User user = new User(email, TestUtil.generateRandomPasswordHash(), name);
        setField(user, "id", id);

        return user;
    }

    private Workspace createWorkspace(Long id, String name, User member) {
        Workspace workspace = new Workspace(name);
        setField(workspace, "id", id);

        workspace.setMembers(new ArrayList<>(List.of(member)));

        return workspace;
    }

    private Folder createFolder(Long id, String name, Workspace workspace) {
        Folder folder = new Folder(name, workspace);
        setField(folder, "id", id);

        return folder;
    }

    private Request createRequest(Long id, String name, RequestMethod method, String url, Folder folder) {
        Request request = new Request(name, method, url, folder);
        setField(request, "id", id);

        return request;
    }
}
