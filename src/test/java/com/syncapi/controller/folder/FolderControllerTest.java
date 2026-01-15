package com.syncapi.controller.folder;

import com.syncapi.JsonTestUtil;
import com.syncapi.TestUtil;
import com.syncapi.dto.folder.FolderRequest;
import com.syncapi.dto.folder.FolderResponse;
import com.syncapi.security.jwt.JwtService;
import com.syncapi.service.folder.FolderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = FolderController.class)
@AutoConfigureMockMvc(addFilters = false)
class FolderControllerTest {
    private static final String FOLDERS_URL = "/api/workspaces/{workspaceId}/folders";
    private static final String WORKSPACE_ID_PLACEHOLDER = "{workspaceId}";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FolderService folderService;

    @MockitoBean
    private JwtService jwtService;

    private String token;
    private Long workspaceId;

    @BeforeEach
    void setUp() {
        reset(folderService);

        token = TestUtil.generateRandomToken();
        workspaceId = TestUtil.generateRandomId();

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(TestUtil.generateRandomEmail(), null, List.of())
        );
    }

    @Test
    void shouldGetFoldersByWorkspace() throws Exception {
        // given
        FolderResponse folder1 = createFolderResponse();
        FolderResponse folder2 = createFolderResponse();

        when(folderService.getFoldersByWorkspace(eq(workspaceId), anyString()))
                .thenReturn(List.of(folder1, folder2));

        // when
        ResultActions res = mockMvc.perform(JsonTestUtil.getJsonAuth(
                FOLDERS_URL.replace(WORKSPACE_ID_PLACEHOLDER, workspaceId.toString()), token
        ));

        // then
        res.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpectAll(
                        jsonPath("$[0].id").value(folder1.getId()),
                        jsonPath("$[0].name").value(folder1.getName()),
                        jsonPath("$[1].id").value(folder2.getId()),
                        jsonPath("$[1].name").value(folder2.getName())
                );

        verify(folderService).getFoldersByWorkspace(eq(workspaceId), anyString());
    }

    @Test
    void shouldReturnEmptyListWhenNoFolders() throws Exception {
        // given
        when(folderService.getFoldersByWorkspace(eq(workspaceId), anyString()))
                .thenReturn(List.of());

        // when
        ResultActions res = mockMvc.perform(JsonTestUtil.getJsonAuth(
                FOLDERS_URL.replace(WORKSPACE_ID_PLACEHOLDER, workspaceId.toString()), token
        ));

        // then
        res.andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void shouldReturnForbiddenWhenGetFoldersThrows() throws Exception {
        // given
        when(folderService.getFoldersByWorkspace(eq(workspaceId), anyString()))
                .thenThrow(new RuntimeException("access denied"));

        // when / then
        mockMvc.perform(JsonTestUtil.getJsonAuth(
                        FOLDERS_URL.replace(WORKSPACE_ID_PLACEHOLDER, workspaceId.toString()), token
                ))
                .andExpect(status().isForbidden());

        verify(folderService).getFoldersByWorkspace(eq(workspaceId), anyString());
    }

    @Test
    void shouldGetFolderById() throws Exception {
        // given
        Long folderId = TestUtil.generateRandomId();
        FolderResponse folder = createFolderResponse(folderId);

        when(folderService.getFolderById(eq(folderId), anyString()))
                .thenReturn(folder);

        // when
        ResultActions res = mockMvc.perform(JsonTestUtil.getJsonAuth(
                FOLDERS_URL.replace(WORKSPACE_ID_PLACEHOLDER, workspaceId.toString()) + "/" + folderId, token
        ));

        // then
        res.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpectAll(
                        jsonPath("$.id").value(folderId),
                        jsonPath("$.name").value(folder.getName()),
                        jsonPath("$.description").value(folder.getDescription()),
                        jsonPath("$.createdAt").exists(),
                        jsonPath("$.workspaceId").value(folder.getWorkspaceId()),
                        jsonPath("$.requestCount").value(folder.getRequestCount())
                );

        verify(folderService).getFolderById(eq(folderId), anyString());
    }

    @Test
    void shouldReturnForbiddenWhenGetFolderByIdThrows() throws Exception {
        // given
        Long folderId = TestUtil.generateRandomId();

        when(folderService.getFolderById(eq(folderId), anyString()))
                .thenThrow(new RuntimeException("access denied"));

        // when / then
        mockMvc.perform(JsonTestUtil.getJsonAuth(
                        FOLDERS_URL.replace(WORKSPACE_ID_PLACEHOLDER, workspaceId.toString()) + "/" + folderId, token
                ))
                .andExpect(status().isForbidden());

        verify(folderService).getFolderById(eq(folderId), anyString());
    }

    @Test
    void shouldCreateFolder() throws Exception {
        // given
        String folderName = TestUtil.generateRandomName();
        String folderDescription = TestUtil.generateRandomValue("description");
        FolderRequest request = new FolderRequest(folderName, folderDescription);

        Long folderId = TestUtil.generateRandomId();
        FolderResponse response = new FolderResponse(
                folderId, folderName, folderDescription, LocalDateTime.now(), workspaceId, 0
        );

        when(folderService.createFolder(eq(workspaceId), any(FolderRequest.class), anyString()))
                .thenReturn(response);

        // when
        ResultActions res = mockMvc.perform(JsonTestUtil.postJsonAuth(
                FOLDERS_URL.replace(WORKSPACE_ID_PLACEHOLDER, workspaceId.toString()), request, token
        ));

        // then
        res.andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpectAll(
                        jsonPath("$.id").value(folderId),
                        jsonPath("$.name").value(folderName),
                        jsonPath("$.description").value(folderDescription),
                        jsonPath("$.workspaceId").value(workspaceId),
                        jsonPath("$.requestCount").value(0)
                );

        verify(folderService).createFolder(eq(workspaceId), any(FolderRequest.class), anyString());
    }

    @Test
    void shouldCreateFolderWithoutDescription() throws Exception {
        // given
        String folderName = TestUtil.generateRandomName();
        FolderRequest request = new FolderRequest(folderName);

        Long folderId = TestUtil.generateRandomId();
        FolderResponse response = new FolderResponse(
                folderId, folderName, null, LocalDateTime.now(), workspaceId, 0
        );

        when(folderService.createFolder(eq(workspaceId), any(FolderRequest.class), anyString()))
                .thenReturn(response);

        // when
        ResultActions res = mockMvc.perform(JsonTestUtil.postJsonAuth(
                FOLDERS_URL.replace(WORKSPACE_ID_PLACEHOLDER, workspaceId.toString()), request, token
        ));

        // then
        res.andExpect(status().isCreated())
                .andExpectAll(
                        jsonPath("$.name").value(folderName),
                        jsonPath("$.description").isEmpty()
                );
    }

    @ParameterizedTest
    @MethodSource("invalidFolderRequests")
    void shouldFailValidationForInvalidCreateFolder(FolderRequest request) throws Exception {
        // when / then
        mockMvc.perform(JsonTestUtil.postJsonAuth(
                        FOLDERS_URL.replace(WORKSPACE_ID_PLACEHOLDER, workspaceId.toString()), request, token
                ))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(folderService);
    }

    private static Stream<FolderRequest> invalidFolderRequests() {
        return Stream.of(
                new FolderRequest(null),
                new FolderRequest(""),
                new FolderRequest("   ")
        );
    }

    @Test
    void shouldReturnForbiddenWhenCreateFolderThrows() throws Exception {
        // given
        FolderRequest request = new FolderRequest(TestUtil.generateRandomName());

        when(folderService.createFolder(eq(workspaceId), any(FolderRequest.class), anyString()))
                .thenThrow(new RuntimeException("access denied"));

        // when / then
        mockMvc.perform(JsonTestUtil.postJsonAuth(
                        FOLDERS_URL.replace(WORKSPACE_ID_PLACEHOLDER, workspaceId.toString()), request, token
                ))
                .andExpect(status().isForbidden());

        verify(folderService).createFolder(eq(workspaceId), any(FolderRequest.class), anyString());
    }

    @Test
    void shouldUpdateFolder() throws Exception {
        // given
        Long folderId = TestUtil.generateRandomId();
        String newName = TestUtil.generateRandomName();
        String newDescription = TestUtil.generateRandomValue("description");
        FolderRequest request = new FolderRequest(newName, newDescription);

        FolderResponse response = new FolderResponse(
                folderId, newName, newDescription, LocalDateTime.now(), workspaceId, 3
        );

        when(folderService.updateFolder(eq(folderId), any(FolderRequest.class), anyString()))
                .thenReturn(response);

        // when
        ResultActions res = mockMvc.perform(JsonTestUtil.putJsonAuth(
                FOLDERS_URL.replace(WORKSPACE_ID_PLACEHOLDER, workspaceId.toString()) + "/" + folderId, request, token
        ));

        // then
        res.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpectAll(
                        jsonPath("$.id").value(folderId),
                        jsonPath("$.name").value(newName),
                        jsonPath("$.description").value(newDescription)
                );

        verify(folderService).updateFolder(eq(folderId), any(FolderRequest.class), anyString());
    }

    @ParameterizedTest
    @MethodSource("invalidFolderRequests")
    void shouldFailValidationForInvalidUpdateFolder(FolderRequest request) throws Exception {
        // given
        Long folderId = TestUtil.generateRandomId();

        // when / then
        mockMvc.perform(JsonTestUtil.putJsonAuth(
                        FOLDERS_URL.replace(WORKSPACE_ID_PLACEHOLDER, workspaceId.toString()) + "/" + folderId, request, token
                ))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(folderService);
    }

    @Test
    void shouldReturnForbiddenWhenUpdateFolderThrows() throws Exception {
        // given
        Long folderId = TestUtil.generateRandomId();
        FolderRequest request = new FolderRequest(TestUtil.generateRandomName());

        when(folderService.updateFolder(eq(folderId), any(FolderRequest.class), anyString()))
                .thenThrow(new RuntimeException("access denied"));

        // when / then
        mockMvc.perform(JsonTestUtil.putJsonAuth(
                        FOLDERS_URL.replace(WORKSPACE_ID_PLACEHOLDER, workspaceId.toString()) + "/" + folderId, request, token
                ))
                .andExpect(status().isForbidden());

        verify(folderService).updateFolder(eq(folderId), any(FolderRequest.class), anyString());
    }

    @Test
    void shouldPatchFolder() throws Exception {
        // given
        String newName = TestUtil.generateRandomName();
        FolderRequest request = new FolderRequest();
        request.setName(newName);

        Long folderId = TestUtil.generateRandomId();
        FolderResponse response = new FolderResponse(folderId, newName,
                TestUtil.generateRandomValue("description"), LocalDateTime.now(), workspaceId, 5);

        when(folderService.patchFolder(eq(folderId), any(FolderRequest.class), anyString()))
                .thenReturn(response);

        // when
        ResultActions res = mockMvc.perform(JsonTestUtil.patchJsonAuth(
                FOLDERS_URL.replace(WORKSPACE_ID_PLACEHOLDER, workspaceId.toString()) + "/" + folderId, request, token
        ));

        // then
        res.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpectAll(
                        jsonPath("$.id").value(folderId),
                        jsonPath("$.name").value(newName)
                );

        verify(folderService).patchFolder(eq(folderId), any(FolderRequest.class), anyString());
    }

    @Test
    void shouldReturnForbiddenWhenPatchFolderThrows() throws Exception {
        // given
        FolderRequest request = new FolderRequest();
        request.setName(TestUtil.generateRandomName());

        Long folderId = TestUtil.generateRandomId();

        when(folderService.patchFolder(eq(folderId), any(FolderRequest.class), anyString()))
                .thenThrow(new RuntimeException("access denied"));

        // when / then
        mockMvc.perform(JsonTestUtil.patchJsonAuth(
                        FOLDERS_URL.replace(WORKSPACE_ID_PLACEHOLDER, workspaceId.toString()) + "/" + folderId, request, token
                ))
                .andExpect(status().isForbidden());

        verify(folderService).patchFolder(eq(folderId), any(FolderRequest.class), anyString());
    }

    @Test
    void shouldDeleteFolder() throws Exception {
        // given
        Long folderId = TestUtil.generateRandomId();

        doNothing().when(folderService).deleteFolder(eq(folderId), anyString());

        // when / then
        mockMvc.perform(JsonTestUtil.deleteAuth(
                        FOLDERS_URL.replace(WORKSPACE_ID_PLACEHOLDER, workspaceId.toString()) + "/" + folderId, token
                ))
                .andExpect(status().isNoContent());

        verify(folderService).deleteFolder(eq(folderId), anyString());
    }

    @Test
    void shouldReturnForbiddenWhenDeleteFolderThrows() throws Exception {
        // given
        Long folderId = TestUtil.generateRandomId();

        doThrow(new RuntimeException("access denied"))
                .when(folderService).deleteFolder(eq(folderId), anyString());

        // when / then
        mockMvc.perform(JsonTestUtil.deleteAuth(
                        FOLDERS_URL.replace(WORKSPACE_ID_PLACEHOLDER, workspaceId.toString()) + "/" + folderId, token
                ))
                .andExpect(status().isForbidden());

        verify(folderService).deleteFolder(eq(folderId), anyString());
    }

    private FolderResponse createFolderResponse() {
        return createFolderResponse(TestUtil.generateRandomId());
    }

    private FolderResponse createFolderResponse(Long folderId) {
        return new FolderResponse(
                folderId,
                TestUtil.generateRandomName(),
                TestUtil.generateRandomValue("description"),
                LocalDateTime.now(),
                workspaceId,
                TestUtil.generateRandomInt() % 10
        );
    }
}
