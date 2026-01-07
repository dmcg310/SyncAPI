package com.syncapi.repository;

import com.syncapi.AbstractIntegrationTest;
import com.syncapi.entity.Folder;
import com.syncapi.entity.Request;
import com.syncapi.entity.User;
import com.syncapi.entity.Workspace;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

        User user = userRepository.save(new User("test@example.com", "hash", "Test User"));
        Workspace workspace = new Workspace("Test Workspace");
        workspace.getMembers().add(user);
        workspace = workspaceRepository.save(workspace);

        folder = new Folder("Test Folder", workspace);
        folder = folderRepository.save(folder);

        request1 = new Request("Get Users", "GET", "https://api.example.com/users", folder);
        request2 = new Request("Create User", "POST", "https://api.example.com/users", folder);

        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        request2.setHeaders(headers);

        Map<String, Object> body = new HashMap<>();
        body.put("name", "John");
        body.put("email", "john@example.com");
        request2.setBody(body);

        requestRepository.save(request1);
        requestRepository.save(request2);
    }

    @Test
    void shouldSaveRequest() {
        // given
        Request request = new Request("Delete User", "DELETE", "https://api.example.com/users/1", folder);

        // when
        Request saved = requestRepository.save(request);

        // then
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getName()).isEqualTo("Delete User");
        assertThat(saved.getMethod()).isEqualTo("DELETE");
        assertThat(saved.getUrl()).isEqualTo("https://api.example.com/users/1");
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
    }

    @Test
    void shouldSaveRequestWithJsonFields() {
        // given
        Request request = new Request("Update User", "PUT", "https://api.example.com/users/1", folder);

        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer token");
        headers.put("Content-Type", "application/json");
        request.setHeaders(headers);

        Map<String, Object> body = new HashMap<>();
        body.put("name", "Jane");
        body.put("age", 25);
        request.setBody(body);

        Map<String, String> authConfig = new HashMap<>();
        authConfig.put("type", "bearer");
        authConfig.put("token", "secret-token");
        request.setAuthConfig(authConfig);

        // when
        Request saved = requestRepository.save(request);

        // then
        assertThat(saved.getHeaders()).hasSize(2);
        assertThat(saved.getHeaders().get("Authorization")).isEqualTo("Bearer token");
        assertThat(saved.getBody()).hasSize(2);
        assertThat(saved.getBody().get("name")).isEqualTo("Jane");
        assertThat(saved.getAuthConfig()).hasSize(2);
    }

    @Test
    void shouldFindRequestsByFolderId() {
        // when
        List<Request> requests = requestRepository.findByFolderId(folder.getId());

        // then
        assertThat(requests).hasSize(2);
        assertThat(requests).extracting(Request::getName)
                .containsExactlyInAnyOrder("Get Users", "Create User");
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
        assertThat(found.getName()).isEqualTo("Get Users");
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
        var originalUpdatedAt = request.getUpdatedAt();

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
        folderRepository.delete(folder);

        // then
        assertThat(requestRepository.findById(requestId)).isEmpty();
    }
}