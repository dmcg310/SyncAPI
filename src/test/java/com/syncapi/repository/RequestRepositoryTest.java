package com.syncapi.repository;

import com.syncapi.AbstractIntegrationTest;
import com.syncapi.TestUtil;
import com.syncapi.entity.Folder;
import com.syncapi.entity.Request;
import com.syncapi.entity.User;
import com.syncapi.entity.Workspace;
import com.syncapi.util.RequestMethod;
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
    private static final String AUTH_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String CONTENT_TYPE_HEADER = "Content-Type";
    private static final String APPLICATION_JSON = "application/json";

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

        Workspace workspace = new Workspace(TestUtil.generateRandomName());
        workspace.getMembers().add(user);
        workspace = workspaceRepository.save(workspace);

        folder = new Folder(TestUtil.generateRandomName(), workspace);
        folder = folderRepository.save(folder);

        request1 = new Request("Get Users", RequestMethod.GET, "https://api.example.com/users", folder);
        requestRepository.save(request1);

        request2 = new Request("Create User", RequestMethod.POST, "https://api.example.com/users", folder);

        Map<String, String> headers = new HashMap<>();
        headers.put(CONTENT_TYPE_HEADER, APPLICATION_JSON);
        request2.setHeaders(headers);

        Map<String, Object> body = new HashMap<>();
        body.put("name", TestUtil.generateRandomName());
        body.put("email", TestUtil.generateRandomEmail());
        request2.setBody(body);

        requestRepository.save(request2);
    }

    @Test
    void shouldSaveRequest() {
        // given
        String name = "Delete User";
        RequestMethod method = RequestMethod.DELETE;
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
        Request request = new Request("Update User", RequestMethod.PUT, "https://api.example.com/users/1", folder);

        String authorizationValue = BEARER_PREFIX + TestUtil.generateRandomToken();
        Map<String, String> headers = new HashMap<>();
        headers.put(AUTH_HEADER, authorizationValue);
        headers.put(CONTENT_TYPE_HEADER, APPLICATION_JSON);
        request.setHeaders(headers);

        String bodyNameValue = TestUtil.generateRandomName();
        int bodyAgeValue = TestUtil.generateRandomInt();
        Map<String, Object> body = new HashMap<>();
        body.put("name", bodyNameValue);
        body.put("age", bodyAgeValue);
        request.setBody(body);

        String authTypeValue = TestUtil.generateRandomValue("authType");
        String authTokenValue = TestUtil.generateRandomToken();
        Map<String, String> authConfig = new HashMap<>();
        authConfig.put("type", authTypeValue);
        authConfig.put("token", authTokenValue);
        request.setAuthConfig(authConfig);

        // when
        Request saved = requestRepository.save(request);

        // then
        assertThat(saved.getHeaders()).hasSize(headers.size());
        assertThat(saved.getHeaders().get("Authorization")).isEqualTo(authorizationValue);
        assertThat(saved.getHeaders().get("Content-Type")).isEqualTo(APPLICATION_JSON);

        assertThat(saved.getBody()).hasSize(body.size());
        assertThat(saved.getBody().get("name")).isEqualTo(bodyNameValue);
        assertThat(saved.getBody().get("age")).isEqualTo(bodyAgeValue);

        assertThat(saved.getAuthConfig()).hasSize(authConfig.size());
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
        Folder emptyFolder = folderRepository.save(new Folder(TestUtil.generateRandomName(), folder.getWorkspace()));

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
        Folder anotherFolder = folderRepository.save(new Folder(TestUtil.generateRandomName(), folder.getWorkspace()));

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
        request.setName(TestUtil.generateRandomName());
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