package com.syncapi.service.documentation;

import com.syncapi.TestUtil;
import com.syncapi.dto.documentation.OpenApiOperation;
import com.syncapi.dto.documentation.OpenApiPathItem;
import com.syncapi.dto.documentation.OpenApiServer;
import com.syncapi.dto.documentation.OpenApiSpec;
import com.syncapi.entity.folder.Folder;
import com.syncapi.entity.request.Request;
import com.syncapi.entity.user.User;
import com.syncapi.entity.workspace.Workspace;
import com.syncapi.exception.AccessDeniedException;
import com.syncapi.exception.ResourceNotFoundException;
import com.syncapi.repository.folder.FolderRepository;
import com.syncapi.repository.request.RequestRepository;
import com.syncapi.util.RequestMethod;
import com.syncapi.util.Util;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DocumentationServiceTest {
    private static final String OPENAPI_VERSION = "3.0.0";

    @Mock
    private FolderRepository folderRepository;

    @Mock
    private RequestRepository requestRepository;

    @Mock
    private Util util;

    private DocumentationService documentationService;

    private String email;
    private Workspace workspace;
    private Folder folder;

    @BeforeEach
    void setUp() {
        documentationService = new DocumentationService(folderRepository, requestRepository, util);

        email = TestUtil.generateRandomEmail();
        User user = TestUtil.createUser(TestUtil.generateRandomId(), email, TestUtil.generateRandomName());
        workspace = TestUtil.createRandomWorkspace(user);
        folder = TestUtil.createRandomFolder(workspace);
    }

    @Test
    void shouldGenerateSpecForWorkspaceWithSingleGetRequest() {
        // given
        Request request = TestUtil.createRequest(TestUtil.generateRandomId(), "Get Users", RequestMethod.GET,
                "https://api.example.com/users", folder);

        when(util.getWorkspaceWithAccessCheck(workspace.getId(), email)).thenReturn(workspace);
        when(folderRepository.findByWorkspaceId(workspace.getId())).thenReturn(List.of(folder));
        when(requestRepository.findByFolderId(folder.getId())).thenReturn(List.of(request));

        // when
        OpenApiSpec spec = documentationService.generateSpec(workspace.getId(), email);

        // then
        assertThat(spec.getOpenapi()).isEqualTo(OPENAPI_VERSION);
        assertThat(spec.getInfo().getTitle()).isEqualTo(workspace.getName());
        assertThat(spec.getInfo().getDescription()).isEqualTo(workspace.getDescription());
        assertThat(spec.getServers()).isNotNull();
        assertThat(spec.getServers()).hasSize(1);
        assertThat(spec.getServers().getFirst().getUrl()).isEqualTo("https://api.example.com");
        assertThat(spec.getPaths()).containsKey("/users");

        OpenApiPathItem pathItem = spec.getPaths().get("/users");
        assertThat(pathItem.getGet()).isNotNull();
        assertThat(pathItem.getGet().getSummary()).isEqualTo("Get Users");
        assertThat(pathItem.getPost()).isNull();
    }

    @Test
    void shouldGenerateSpecForWorkspaceWithMultipleRequestsDifferentMethods() {
        // given
        Request getRequest = TestUtil.createRequest(TestUtil.generateRandomId(), "Get User", RequestMethod.GET,
                "https://api.example.com/users/1", folder);

        Request postRequest = TestUtil.createRequest(TestUtil.generateRandomId(), "Create User",
                RequestMethod.POST, "https://api.example.com/users", folder);
        postRequest.setBody(Map.of("name", "John"));

        Request deleteRequest = TestUtil.createRequest(TestUtil.generateRandomId(), "Delete User",
                RequestMethod.DELETE, "https://api.example.com/users/1", folder);

        when(util.getWorkspaceWithAccessCheck(workspace.getId(), email)).thenReturn(workspace);
        when(folderRepository.findByWorkspaceId(workspace.getId())).thenReturn(List.of(folder));
        when(requestRepository.findByFolderId(folder.getId())).thenReturn(List.of(getRequest, postRequest, deleteRequest));

        // when
        OpenApiSpec spec = documentationService.generateSpec(workspace.getId(), email);

        // then
        assertThat(spec.getPaths()).hasSize(2);
        assertThat(spec.getPaths()).containsKeys("/users/{id}", "/users");

        OpenApiPathItem usersIdPath = spec.getPaths().get("/users/{id}");
        assertThat(usersIdPath.getGet()).isNotNull();
        assertThat(usersIdPath.getDelete()).isNotNull();

        OpenApiPathItem usersPath = spec.getPaths().get("/users");
        assertThat(usersPath.getPost()).isNotNull();
    }

    @Test
    void shouldGenerateSpecForPostRequestWithBodySchemaInference() {
        // given
        Map<String, Object> requestBody = Map.of(
                "name", "John",
                "age", 30,
                "active", true
        );

        Request postRequest = TestUtil.createRequest(TestUtil.generateRandomId(), "Create User",
                RequestMethod.POST, "https://api.example.com/users", folder);
        postRequest.setBody(requestBody);

        when(util.getWorkspaceWithAccessCheck(workspace.getId(), email)).thenReturn(workspace);
        when(folderRepository.findByWorkspaceId(workspace.getId())).thenReturn(List.of(folder));
        when(requestRepository.findByFolderId(folder.getId())).thenReturn(List.of(postRequest));

        // when
        OpenApiSpec spec = documentationService.generateSpec(workspace.getId(), email);

        // then
        OpenApiOperation operation = spec.getPaths().get("/users").getPost();
        assertThat(operation.getRequestBody()).isNotNull();
        assertThat(operation.getRequestBody().isRequired()).isTrue();
        assertThat(operation.getRequestBody().getContent()).containsKey("application/json");

        var mediaType = operation.getRequestBody().getContent().get("application/json");
        assertThat(mediaType.getSchema().getType()).isEqualTo("object");
        assertThat(mediaType.getSchema().getProperties()).containsKeys("name", "age", "active");
        assertThat(mediaType.getSchema().getProperties().get("name").getType()).isEqualTo("string");
        assertThat(mediaType.getSchema().getProperties().get("age").getType()).isEqualTo("integer");
        assertThat(mediaType.getSchema().getProperties().get("active").getType()).isEqualTo("boolean");
        assertThat(mediaType.getExample()).isEqualTo(requestBody);
    }

    @Test
    void shouldNormalizeNumericPathSegmentsToParameters() {
        // given
        Request request = TestUtil.createRequest(TestUtil.generateRandomId(), "Get User Post", RequestMethod.GET,
                "https://api.example.com/users/123/posts/456", folder);

        when(util.getWorkspaceWithAccessCheck(workspace.getId(), email)).thenReturn(workspace);
        when(folderRepository.findByWorkspaceId(workspace.getId())).thenReturn(List.of(folder));
        when(requestRepository.findByFolderId(folder.getId())).thenReturn(List.of(request));

        // when
        OpenApiSpec spec = documentationService.generateSpec(workspace.getId(), email);

        // then
        assertThat(spec.getPaths()).containsKey("/users/{id}/posts/{id}");

        OpenApiOperation operation = spec.getPaths().get("/users/{id}/posts/{id}").getGet();
        assertThat(operation.getParameters()).hasSize(2);
        assertThat(operation.getParameters()).allMatch(p -> p.getIn().equals("path"));
        assertThat(operation.getParameters()).allMatch(p -> p.isRequired());
    }

    @Test
    void shouldExtractQueryParameters() {
        // given
        Request request = TestUtil.createRequest(TestUtil.generateRandomId(), "Search Users", RequestMethod.GET,
                "https://api.example.com/users?page=1&limit=10&sort=name", folder);

        when(util.getWorkspaceWithAccessCheck(workspace.getId(), email)).thenReturn(workspace);
        when(folderRepository.findByWorkspaceId(workspace.getId())).thenReturn(List.of(folder));
        when(requestRepository.findByFolderId(folder.getId())).thenReturn(List.of(request));

        // when
        OpenApiSpec spec = documentationService.generateSpec(workspace.getId(), email);

        // then
        OpenApiOperation operation = spec.getPaths().get("/users").getGet();
        assertThat(operation.getParameters()).hasSize(3);

        var queryParams = operation.getParameters().stream()
                .filter(p -> p.getIn().equals("query"))
                .toList();

        assertThat(queryParams).hasSize(3);
        assertThat(queryParams).extracting("name").containsExactlyInAnyOrder("page", "limit", "sort");
        assertThat(queryParams).allMatch(p -> !p.isRequired());
    }

    @Test
    void shouldReturnValidSpecWithEmptyPathsForEmptyWorkspace() {
        // given
        when(util.getWorkspaceWithAccessCheck(workspace.getId(), email)).thenReturn(workspace);
        when(folderRepository.findByWorkspaceId(workspace.getId())).thenReturn(List.of());

        // when
        OpenApiSpec spec = documentationService.generateSpec(workspace.getId(), email);

        // then
        assertThat(spec.getOpenapi()).isEqualTo(OPENAPI_VERSION);
        assertThat(spec.getInfo().getTitle()).isEqualTo(workspace.getName());
        assertThat(spec.getServers()).isNull();
        assertThat(spec.getPaths()).isEmpty();
    }

    @Test
    void shouldInferNestedObjectSchema() {
        // given
        Map<String, Object> nestedBody = Map.of(
                "user", Map.of(
                        "name", "John",
                        "address", Map.of(
                                "city", "London",
                                "postcode", "SW1A 1AA"
                        )
                )
        );

        Request postRequest = TestUtil.createRequest(TestUtil.generateRandomId(), "Create User",
                RequestMethod.POST, "https://api.example.com/users", folder);
        postRequest.setBody(nestedBody);

        when(util.getWorkspaceWithAccessCheck(workspace.getId(), email)).thenReturn(workspace);
        when(folderRepository.findByWorkspaceId(workspace.getId())).thenReturn(List.of(folder));
        when(requestRepository.findByFolderId(folder.getId())).thenReturn(List.of(postRequest));

        // when
        OpenApiSpec spec = documentationService.generateSpec(workspace.getId(), email);

        // then
        var schema = spec.getPaths().get("/users").getPost()
                .getRequestBody().getContent().get("application/json").getSchema();

        assertThat(schema.getType()).isEqualTo("object");
        assertThat(schema.getProperties().get("user").getType()).isEqualTo("object");
        assertThat(schema.getProperties().get("user").getProperties().get("name").getType()).isEqualTo("string");
        assertThat(schema.getProperties().get("user").getProperties().get("address").getType()).isEqualTo("object");
        assertThat(schema.getProperties().get("user").getProperties().get("address")
                .getProperties().get("city").getType()).isEqualTo("string");
    }

    @Test
    void shouldInferArraySchema() {
        // given
        Map<String, Object> bodyWithArray = Map.of(
                "tags", List.of("java", "spring", "api"),
                "scores", List.of(1, 2, 3)
        );

        Request postRequest = TestUtil.createRequest(TestUtil.generateRandomId(), "Create Item",
                RequestMethod.POST, "https://api.example.com/items", folder);
        postRequest.setBody(bodyWithArray);

        when(util.getWorkspaceWithAccessCheck(workspace.getId(), email)).thenReturn(workspace);
        when(folderRepository.findByWorkspaceId(workspace.getId())).thenReturn(List.of(folder));
        when(requestRepository.findByFolderId(folder.getId())).thenReturn(List.of(postRequest));

        // when
        OpenApiSpec spec = documentationService.generateSpec(workspace.getId(), email);

        // then
        var schema = spec.getPaths().get("/items").getPost()
                .getRequestBody().getContent().get("application/json").getSchema();

        assertThat(schema.getProperties().get("tags").getType()).isEqualTo("array");
        assertThat(schema.getProperties().get("tags").getItems().getType()).isEqualTo("string");
        assertThat(schema.getProperties().get("scores").getType()).isEqualTo("array");
        assertThat(schema.getProperties().get("scores").getItems().getType()).isEqualTo("integer");
    }

    @Test
    void shouldNotIncludeRequestBodyForGetRequest() {
        // given
        Request getRequest = TestUtil.createRequest(TestUtil.generateRandomId(), "Get User", RequestMethod.GET,
                "https://api.example.com/users", folder);
        getRequest.setBody(Map.of("ignored", "body"));

        when(util.getWorkspaceWithAccessCheck(workspace.getId(), email)).thenReturn(workspace);
        when(folderRepository.findByWorkspaceId(workspace.getId())).thenReturn(List.of(folder));
        when(requestRepository.findByFolderId(folder.getId())).thenReturn(List.of(getRequest));

        // when
        OpenApiSpec spec = documentationService.generateSpec(workspace.getId(), email);

        // then
        assertThat(spec.getPaths().get("/users").getGet().getRequestBody()).isNull();
    }

    @Test
    void shouldNotIncludeRequestBodyForDeleteRequest() {
        // given
        Request deleteRequest = TestUtil.createRequest(TestUtil.generateRandomId(), "Delete User",
                RequestMethod.DELETE, "https://api.example.com/users/1", folder);
        deleteRequest.setBody(Map.of("ignored", "body"));

        when(util.getWorkspaceWithAccessCheck(workspace.getId(), email)).thenReturn(workspace);
        when(folderRepository.findByWorkspaceId(workspace.getId())).thenReturn(List.of(folder));
        when(requestRepository.findByFolderId(folder.getId())).thenReturn(List.of(deleteRequest));

        // when
        OpenApiSpec spec = documentationService.generateSpec(workspace.getId(), email);

        // then
        assertThat(spec.getPaths().get("/users/{id}").getDelete().getRequestBody()).isNull();
    }

    @Test
    void shouldThrowWhenWorkspaceNotFound() {
        // given
        Long workspaceId = TestUtil.generateRandomId();

        when(util.getWorkspaceWithAccessCheck(workspaceId, email))
                .thenThrow(new ResourceNotFoundException("Workspace not found: " + workspaceId));

        // when / then
        assertThatThrownBy(() -> documentationService.generateSpec(workspaceId, email))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Workspace not found");
    }

    @Test
    void shouldThrowWhenAccessDenied() {
        // given
        User otherUser = TestUtil.createRandomUser();
        Workspace otherWorkspace = TestUtil.createRandomWorkspace(otherUser);

        when(util.getWorkspaceWithAccessCheck(otherWorkspace.getId(), email))
                .thenThrow(new AccessDeniedException("Access denied to workspace: " + otherWorkspace.getId()));

        // when / then
        assertThatThrownBy(() -> documentationService.generateSpec(otherWorkspace.getId(), email))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("Access denied");
    }

    @Test
    void shouldHandleRootPath() {
        // given
        Request request = TestUtil.createRequest(TestUtil.generateRandomId(), "Health Check", RequestMethod.GET,
                "https://api.example.com/", folder);

        when(util.getWorkspaceWithAccessCheck(workspace.getId(), email)).thenReturn(workspace);
        when(folderRepository.findByWorkspaceId(workspace.getId())).thenReturn(List.of(folder));
        when(requestRepository.findByFolderId(folder.getId())).thenReturn(List.of(request));

        // when
        OpenApiSpec spec = documentationService.generateSpec(workspace.getId(), email);

        // then
        assertThat(spec.getPaths()).containsKey("/");
    }

    @Test
    void shouldHandleMalformedUrl() {
        // given
        Request request = TestUtil.createRequest(TestUtil.generateRandomId(), "Bad Request", RequestMethod.GET,
                "not-a-valid-url", folder);

        when(util.getWorkspaceWithAccessCheck(workspace.getId(), email)).thenReturn(workspace);
        when(folderRepository.findByWorkspaceId(workspace.getId())).thenReturn(List.of(folder));
        when(requestRepository.findByFolderId(folder.getId())).thenReturn(List.of(request));

        // when
        OpenApiSpec spec = documentationService.generateSpec(workspace.getId(), email);

        // then
        assertThat(spec.getPaths()).containsKey("/not-a-valid-url");
    }

    @Test
    void shouldIncludeDefaultSuccessResponse() {
        // given
        Request request = TestUtil.createRequest(TestUtil.generateRandomId(), "Get Users", RequestMethod.GET,
                "https://api.example.com/users", folder);

        when(util.getWorkspaceWithAccessCheck(workspace.getId(), email)).thenReturn(workspace);
        when(folderRepository.findByWorkspaceId(workspace.getId())).thenReturn(List.of(folder));
        when(requestRepository.findByFolderId(folder.getId())).thenReturn(List.of(request));

        // when
        OpenApiSpec spec = documentationService.generateSpec(workspace.getId(), email);

        // then
        var responses = spec.getPaths().get("/users").getGet().getResponses();
        assertThat(responses).containsKey("200");
        assertThat(responses.get("200").getDescription()).isEqualTo("Successful response");
    }

    @Test
    void shouldExtractServerFromFullUrl() {
        // given
        Request request = TestUtil.createRequest(TestUtil.generateRandomId(), "Get Posts", RequestMethod.GET,
                "https://jsonplaceholder.typicode.com/posts", folder);

        when(util.getWorkspaceWithAccessCheck(workspace.getId(), email)).thenReturn(workspace);
        when(folderRepository.findByWorkspaceId(workspace.getId())).thenReturn(List.of(folder));
        when(requestRepository.findByFolderId(folder.getId())).thenReturn(List.of(request));

        // when
        OpenApiSpec spec = documentationService.generateSpec(workspace.getId(), email);

        // then
        assertThat(spec.getServers()).isNotNull();
        assertThat(spec.getServers()).hasSize(1);
        assertThat(spec.getServers().getFirst().getUrl()).isEqualTo("https://jsonplaceholder.typicode.com");
        assertThat(spec.getPaths()).containsKey("/posts");
    }

    @Test
    void shouldExtractMultipleServersFromDifferentBaseUrls() {
        // given
        Request request1 = TestUtil.createRequest(TestUtil.generateRandomId(), "Get Users", RequestMethod.GET,
                "https://api.example.com/users", folder);
        Request request2 = TestUtil.createRequest(TestUtil.generateRandomId(), "Get Posts", RequestMethod.GET,
                "https://jsonplaceholder.typicode.com/posts", folder);

        when(util.getWorkspaceWithAccessCheck(workspace.getId(), email)).thenReturn(workspace);
        when(folderRepository.findByWorkspaceId(workspace.getId())).thenReturn(List.of(folder));
        when(requestRepository.findByFolderId(folder.getId())).thenReturn(List.of(request1, request2));

        // when
        OpenApiSpec spec = documentationService.generateSpec(workspace.getId(), email);

        // then
        assertThat(spec.getServers()).isNotNull();
        assertThat(spec.getServers()).hasSize(2);
        assertThat(spec.getServers()).extracting(OpenApiServer::getUrl)
                .containsExactlyInAnyOrder("https://api.example.com", "https://jsonplaceholder.typicode.com");
    }

    @Test
    void shouldHandleVariablePlaceholderUrls() {
        // given
        Request request1 = TestUtil.createRequest(TestUtil.generateRandomId(), "Login", RequestMethod.POST,
                "{{BASE_URL}}/api/auth/login", folder);
        Request request2 = TestUtil.createRequest(TestUtil.generateRandomId(), "Update Password", RequestMethod.PATCH,
                "{{BASE_URL}}/api/auth/password", folder);

        when(util.getWorkspaceWithAccessCheck(workspace.getId(), email)).thenReturn(workspace);
        when(folderRepository.findByWorkspaceId(workspace.getId())).thenReturn(List.of(folder));
        when(requestRepository.findByFolderId(folder.getId())).thenReturn(List.of(request1, request2));

        // when
        OpenApiSpec spec = documentationService.generateSpec(workspace.getId(), email);

        // then
        assertThat(spec.getServers()).isNotNull();
        assertThat(spec.getServers()).hasSize(1);
        assertThat(spec.getServers().getFirst().getUrl()).isEmpty();
        assertThat(spec.getServers().getFirst().getDescription()).isEqualTo("Variable base URL (from environment)");
        assertThat(spec.getPaths()).containsKeys("/api/auth/login", "/api/auth/password");
    }

    @Test
    void shouldHandleMixOfVariableAndFullUrls() {
        // given
        Request request1 = TestUtil.createRequest(TestUtil.generateRandomId(), "Login", RequestMethod.POST,
                "{{BASE_URL}}/api/auth/login", folder);
        Request request2 = TestUtil.createRequest(TestUtil.generateRandomId(), "Get Posts", RequestMethod.GET,
                "https://jsonplaceholder.typicode.com/posts", folder);

        when(util.getWorkspaceWithAccessCheck(workspace.getId(), email)).thenReturn(workspace);
        when(folderRepository.findByWorkspaceId(workspace.getId())).thenReturn(List.of(folder));
        when(requestRepository.findByFolderId(folder.getId())).thenReturn(List.of(request1, request2));

        // when
        OpenApiSpec spec = documentationService.generateSpec(workspace.getId(), email);

        // then
        assertThat(spec.getServers()).isNotNull();
        assertThat(spec.getServers()).hasSize(2);
        assertThat(spec.getServers().getFirst().getUrl()).isEmpty();
        assertThat(spec.getServers().getFirst().getDescription()).isEqualTo("Variable base URL (from environment)");
        assertThat(spec.getServers().get(1).getUrl()).isEqualTo("https://jsonplaceholder.typicode.com");
        assertThat(spec.getPaths()).containsKeys("/api/auth/login", "/posts");
    }

    @Test
    void shouldExtractPathFromVariablePlaceholderUrl() {
        // given
        Request request = TestUtil.createRequest(TestUtil.generateRandomId(), "Login", RequestMethod.POST,
                "{{BASE_URL}}/api/auth/login", folder);

        when(util.getWorkspaceWithAccessCheck(workspace.getId(), email)).thenReturn(workspace);
        when(folderRepository.findByWorkspaceId(workspace.getId())).thenReturn(List.of(folder));
        when(requestRepository.findByFolderId(folder.getId())).thenReturn(List.of(request));

        // when
        OpenApiSpec spec = documentationService.generateSpec(workspace.getId(), email);

        // then
        assertThat(spec.getPaths()).containsKey("/api/auth/login");
        assertThat(spec.getPaths().get("/api/auth/login").getPost()).isNotNull();
    }

    @Test
    void shouldHandleVariablePlaceholderWithRootPath() {
        // given
        Request request = TestUtil.createRequest(TestUtil.generateRandomId(), "Health Check", RequestMethod.GET,
                "{{BASE_URL}}/", folder);

        when(util.getWorkspaceWithAccessCheck(workspace.getId(), email)).thenReturn(workspace);
        when(folderRepository.findByWorkspaceId(workspace.getId())).thenReturn(List.of(folder));
        when(requestRepository.findByFolderId(folder.getId())).thenReturn(List.of(request));

        // when
        OpenApiSpec spec = documentationService.generateSpec(workspace.getId(), email);

        // then
        assertThat(spec.getPaths()).containsKey("/");
        assertThat(spec.getServers()).isNotNull();
        assertThat(spec.getServers().getFirst().getDescription()).isEqualTo("Variable base URL (from environment)");
    }

    @Test
    void shouldExtractServerWithPort() {
        // given
        Request request = TestUtil.createRequest(TestUtil.generateRandomId(), "Get Users", RequestMethod.GET,
                "http://localhost:8080/api/users", folder);

        when(util.getWorkspaceWithAccessCheck(workspace.getId(), email)).thenReturn(workspace);
        when(folderRepository.findByWorkspaceId(workspace.getId())).thenReturn(List.of(folder));
        when(requestRepository.findByFolderId(folder.getId())).thenReturn(List.of(request));

        // when
        OpenApiSpec spec = documentationService.generateSpec(workspace.getId(), email);

        // then
        assertThat(spec.getServers()).isNotNull();
        assertThat(spec.getServers()).hasSize(1);
        assertThat(spec.getServers().getFirst().getUrl()).isEqualTo("http://localhost:8080");
        assertThat(spec.getPaths()).containsKey("/api/users");
    }

    @Test
    void shouldDeduplicateServersWithSameBaseUrl() {
        // given
        Request request1 = TestUtil.createRequest(TestUtil.generateRandomId(), "Get Users", RequestMethod.GET,
                "https://api.example.com/users", folder);
        Request request2 = TestUtil.createRequest(TestUtil.generateRandomId(), "Get Posts", RequestMethod.GET,
                "https://api.example.com/posts", folder);

        when(util.getWorkspaceWithAccessCheck(workspace.getId(), email)).thenReturn(workspace);
        when(folderRepository.findByWorkspaceId(workspace.getId())).thenReturn(List.of(folder));
        when(requestRepository.findByFolderId(folder.getId())).thenReturn(List.of(request1, request2));

        // when
        OpenApiSpec spec = documentationService.generateSpec(workspace.getId(), email);

        // then
        assertThat(spec.getServers()).isNotNull();
        assertThat(spec.getServers()).hasSize(1);
        assertThat(spec.getServers().getFirst().getUrl()).isEqualTo("https://api.example.com");
    }

    @Test
    void shouldReturnNullServersForEmptyWorkspace() {
        // given
        when(util.getWorkspaceWithAccessCheck(workspace.getId(), email)).thenReturn(workspace);
        when(folderRepository.findByWorkspaceId(workspace.getId())).thenReturn(List.of());

        // when
        OpenApiSpec spec = documentationService.generateSpec(workspace.getId(), email);

        // then
        assertThat(spec.getServers()).isNull();
    }
}
