package com.syncapi.repository;

import com.syncapi.AbstractIntegrationTest;
import com.syncapi.entity.Folder;
import com.syncapi.entity.Request;
import com.syncapi.entity.User;
import com.syncapi.entity.Workspace;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.syncapi.TestUtil.*;
import static org.assertj.core.api.Assertions.assertThat;

class RequestRepositoryTest extends AbstractIntegrationTest {
    @Autowired
    private RequestRepository requestRepository;

    @Autowired
    private FolderRepository folderRepository;

    @Autowired
    private WorkspaceRepository workspaceRepository;

    @Autowired
    private UserRepository userRepository;

    private Folder folder;
    private Request request1, request2;

    @BeforeEach
    void setUp() {
        requestRepository.deleteAll();
        folderRepository.deleteAll();
        workspaceRepository.deleteAll();
        userRepository.deleteAll();

        User user = userRepository.save(new User(generateRandomEmail(), generateRandomPasswordHash(),
                generateRandomName()));

        Workspace workspace = new Workspace("Test Workspace");
        workspace.getMembers().add(user);
        workspace = workspaceRepository.save(workspace);

        folder = new Folder("Test Folder", workspace);
        folder = folderRepository.save(folder);

        request1 = new Request("Get Users", "GET", "https://api.example.com/users", folder);
        requestRepository.save(request1);

        request2 = new Request("Create User", "POST", "https://api.example.com/users", folder);

        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        request2.setHeaders(headers);

        Map<String, Object> body = new HashMap<>();
        body.put("name", "John");
        body.put("email", "john@example.com");
        request2.setBody(body);

        requestRepository.save(request2);
    }

    @Test
    void shouldSaveRequest() {
        // given
        String name = "Delete User";
        String method = "DELETE";
        String url = "https://api.example.com/users/1";
        Request request = new Request(name, method, url, folder);

        // when
        Request saved = requestRepository.save(request);

        // then
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getName()).isEqualTo(name);
        assertThat(saved.getMethod()).isEqualTo(method);
        assertThat(saved.getUrl()).isEqualTo(url);
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
    }

    @Test
    void shouldSaveRequestWithJsonFields() {
        // given
        String name = "Update User";
        String method = "PUT";
        String url = "https://api.example.com/users/1";
        Request request = new Request(name, method, url, folder);

        String authorizationValue = "Bearer token";
        String contentTypeValue = "application/json";

        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", authorizationValue);
        headers.put("Content-Type", contentTypeValue);
        request.setHeaders(headers);

        String bodyNameValue = "Jane";
        int bodyAgeValue = 25;

        Map<String, Object> body = new HashMap<>();
        body.put("name", bodyNameValue);
        body.put("age", bodyAgeValue);
        request.setBody(body);

        String authTypeValue = "bearer";
        String authTokenValue = "secret-token";

        Map<String, String> authConfig = new HashMap<>();
        authConfig.put("type", authTypeValue);
        authConfig.put("token", authTokenValue);
        request.setAuthConfig(authConfig);

        // when
        Request saved = requestRepository.save(request);

        // then
        assertThat(saved.getHeaders()).hasSize(2);
        assertThat(saved.getHeaders().get("Authorization")).isEqualTo(authorizationValue);
        assertThat(saved.getHeaders().get("Content-Type")).isEqualTo(contentTypeValue);
        assertThat(saved.getBody()).hasSize(2);
        assertThat(saved.getBody().get("name")).isEqualTo(bodyNameValue);
        assertThat(saved.getBody().get("age")).isEqualTo(bodyAgeValue);
        assertThat(saved.getAuthConfig()).hasSize(2);
        assertThat(saved.getAuthConfig().get("type")).isEqualTo(authTypeValue);
        assertThat(saved.getAuthConfig().get("token")).isEqualTo(authTokenValue);
    }

    @Test
    void shouldFindRequestsByFolderId() {
        // when
        List<Request> requests = requestRepository.findByFolderId(folder.getId());

        // then
        assertThat(requests).hasSize(2);
        assertThat(requests).extracting(Request::getName)
                .containsExactlyInAnyOrder(request1.getName(), request2.getName());
    }

    @Test
    void shouldReturnEmptyListWhenFolderHasNoRequests() {
        // given
        Folder emptyFolder = folderRepository.save(new Folder("Empty Folder", folder.getWorkspace()));

        // when
        List<Request> requests = requestRepository.findByFolderId(emptyFolder.getId());

        // then
        assertThat(requests).isEmpty();
    }

    @Test
    void shouldFindRequestByIdAndFolderId() {
        // when
        Request found = requestRepository.findByIdAndFolderId(request1.getId(), folder.getId());

        // then
        assertThat(found).isNotNull();
        assertThat(found.getName()).isEqualTo(request1.getName());
    }

    @Test
    void shouldReturnNullWhenRequestNotInFolder() {
        // given
        Folder anotherFolder = folderRepository.save(new Folder("Another Folder", folder.getWorkspace()));

        // when
        Request found = requestRepository.findByIdAndFolderId(request1.getId(), anotherFolder.getId());

        // then
        assertThat(found).isNull();
    }

    @Test
    void shouldUpdateTimestampOnUpdate() throws InterruptedException {
        // given
        Request request = requestRepository.findById(request1.getId()).orElseThrow();
        LocalDateTime originalUpdatedAt = request.getUpdatedAt();

        Thread.sleep(10); // ensure time difference

        // when
        request.setName("Updated Name");
        Request updated = requestRepository.save(request);

        // then
        assertThat(updated.getUpdatedAt()).isAfter(originalUpdatedAt);
    }

    @Test
    void shouldCascadeDeleteWhenFolderDeleted() {
        // given
        Long requestId = request1.getId();

        // when
        folderRepository.deleteById(folder.getId());

        // then
        assertThat(requestRepository.findById(requestId)).isEmpty();
    }
}