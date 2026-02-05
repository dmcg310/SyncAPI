package com.syncapi.controller.workspace;

import com.syncapi.JsonTestUtil;
import com.syncapi.TestUtil;
import com.syncapi.dto.workspace.AddMemberRequest;
import com.syncapi.dto.workspace.WorkspaceRequest;
import com.syncapi.dto.workspace.WorkspaceResponse;
import com.syncapi.exception.AccessDeniedException;
import com.syncapi.exception.BadRequestException;
import com.syncapi.security.jwt.JwtService;
import com.syncapi.service.workspace.WorkspaceService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = WorkspaceController.class)
@AutoConfigureMockMvc(addFilters = false)
class WorkspaceControllerTest {
    private static final String WORKSPACES_URL = "/api/workspaces";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private WorkspaceService workspaceService;

    @MockitoBean
    private JwtService jwtService;

    private String token;

    @BeforeEach
    void setUp() {
        reset(workspaceService);

        token = TestUtil.generateRandomToken();

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(TestUtil.generateRandomEmail(), null, List.of())
        );
    }

    @Test
    void shouldGetUserWorkspaces() throws Exception {
        // given
        Long firstWorkspaceId = TestUtil.generateRandomId();
        String firstWorkspaceName = TestUtil.generateRandomName();
        String firstWorkspaceDescription = TestUtil.generateRandomDescription();
        int firstMemberCount = TestUtil.generateRandomInt();
        int firstFolderCount = TestUtil.generateRandomInt();
        int firstEnvironmentCount = TestUtil.generateRandomInt();
        WorkspaceResponse firstWorkspaceResponse = new WorkspaceResponse(firstWorkspaceId, firstWorkspaceName,
                firstWorkspaceDescription, LocalDateTime.now(), firstMemberCount, firstFolderCount,
                firstEnvironmentCount);

        Long secondWorkspaceId = TestUtil.generateRandomId();
        String secondWorkspaceName = TestUtil.generateRandomName();
        String secondWorkspaceDescription = TestUtil.generateRandomDescription();
        int secondMemberCount = TestUtil.generateRandomInt();
        int secondFolderCount = TestUtil.generateRandomInt();
        int secondEnvironmentCount = TestUtil.generateRandomInt();
        WorkspaceResponse secondWorkspaceResponse = new WorkspaceResponse(secondWorkspaceId, secondWorkspaceName,
                secondWorkspaceDescription, LocalDateTime.now(), secondMemberCount, secondFolderCount,
                secondEnvironmentCount);

        when(workspaceService.getUserWorkspaces(anyString()))
                .thenReturn(List.of(firstWorkspaceResponse, secondWorkspaceResponse));

        // when
        ResultActions res = mockMvc.perform(JsonTestUtil.getJsonAuth(WORKSPACES_URL, token));

        // then
        res.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpectAll(
                        jsonPath("$[0].id").value(firstWorkspaceId.intValue()),
                        jsonPath("$[0].name").value(firstWorkspaceName),
                        jsonPath("$[0].description").value(firstWorkspaceDescription),
                        jsonPath("$[0].createdAt").exists(),
                        jsonPath("$[0].memberCount").value(firstMemberCount),
                        jsonPath("$[0].folderCount").value(firstFolderCount),
                        jsonPath("$[0].environmentCount").value(firstEnvironmentCount)
                )
                .andExpectAll(
                        jsonPath("$[1].id").value(secondWorkspaceId.intValue()),
                        jsonPath("$[1].name").value(secondWorkspaceName),
                        jsonPath("$[1].description").value(secondWorkspaceDescription),
                        jsonPath("$[1].createdAt").exists(),
                        jsonPath("$[1].memberCount").value(secondMemberCount),
                        jsonPath("$[1].folderCount").value(secondFolderCount),
                        jsonPath("$[1].environmentCount").value(secondEnvironmentCount)
                );

        verify(workspaceService).getUserWorkspaces(anyString());
    }

    @Test
    void shouldGetWorkspaceById() throws Exception {
        // given
        Long workspaceId = TestUtil.generateRandomId();
        String workspaceName = TestUtil.generateRandomName();
        String workspaceDescription = TestUtil.generateRandomDescription();
        int memberCount = TestUtil.generateRandomInt();
        int folderCount = TestUtil.generateRandomInt();
        int environmentCount = TestUtil.generateRandomInt();
        WorkspaceResponse workspaceResponse = new WorkspaceResponse(workspaceId, workspaceName, workspaceDescription,
                LocalDateTime.now(), memberCount, folderCount, environmentCount);

        when(workspaceService.getWorkspace(eq(workspaceId), anyString()))
                .thenReturn(workspaceResponse);

        // when
        ResultActions res = mockMvc.perform(JsonTestUtil.getJsonAuth(WORKSPACES_URL + "/" + workspaceId, token));

        // then
        res.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpectAll(
                        jsonPath("$.id").value(workspaceId.intValue()),
                        jsonPath("$.name").value(workspaceName),
                        jsonPath("$.description").value(workspaceDescription),
                        jsonPath("$.createdAt").exists(),
                        jsonPath("$.memberCount").value(memberCount),
                        jsonPath("$.folderCount").value(folderCount),
                        jsonPath("$.environmentCount").value(environmentCount)
                );

        verify(workspaceService).getWorkspace(eq(workspaceId), anyString());
    }

    @Test
    void shouldReturnForbiddenWhenGetWorkspaceThrows() throws Exception {
        when(workspaceService.getWorkspace(anyLong(), anyString()))
                .thenThrow(new AccessDeniedException("forbidden"));

        mockMvc.perform(JsonTestUtil.getJsonAuth(WORKSPACES_URL + "/" + TestUtil.generateRandomId(), token))
                .andExpect(status().isForbidden());

        verify(workspaceService).getWorkspace(anyLong(), anyString());
    }

    @Test
    void shouldCreateWorkspace() throws Exception {
        // given
        String workspaceName = TestUtil.generateRandomName();
        WorkspaceRequest request = new WorkspaceRequest(workspaceName);

        Long workspaceId = TestUtil.generateRandomId();
        String workspaceDescription = TestUtil.generateRandomDescription();
        int memberCount = TestUtil.generateRandomInt();
        int folderCount = TestUtil.generateRandomInt();
        int environmentCount = TestUtil.generateRandomInt();
        WorkspaceResponse response = new WorkspaceResponse(workspaceId, workspaceName, workspaceDescription,
                LocalDateTime.now(), memberCount, folderCount, environmentCount);

        when(workspaceService.createWorkspace(any(WorkspaceRequest.class), anyString()))
                .thenReturn(response);

        // when
        ResultActions res = mockMvc.perform(JsonTestUtil.postJsonAuth(WORKSPACES_URL, request, token));

        // then
        res.andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpectAll(
                        jsonPath("$.id").value(workspaceId),
                        jsonPath("$.name").value(workspaceName),
                        jsonPath("$.description").value(workspaceDescription),
                        jsonPath("$.createdAt").exists(),
                        jsonPath("$.memberCount").value(memberCount),
                        jsonPath("$.folderCount").value(folderCount),
                        jsonPath("$.environmentCount").value(environmentCount)
                );

        verify(workspaceService).createWorkspace(any(WorkspaceRequest.class), anyString());
    }

    @ParameterizedTest
    @MethodSource("invalidWorkspaceRequests")
    void shouldFailValidationForInvalidCreateWorkspace(WorkspaceRequest request) throws Exception {
        mockMvc.perform(JsonTestUtil.postJsonAuth(WORKSPACES_URL, request, token))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(workspaceService);
    }

    private static Stream<WorkspaceRequest> invalidWorkspaceRequests() {
        return Stream.of(
                new WorkspaceRequest(null),
                new WorkspaceRequest(""),
                new WorkspaceRequest("   ")
        );
    }

    @Test
    void shouldUpdateWorkspace() throws Exception {
        // given
        WorkspaceRequest request = new WorkspaceRequest(TestUtil.generateRandomName());

        Long workspaceId = TestUtil.generateRandomId();
        String updatedWorkspaceName = TestUtil.generateRandomName();
        String workspaceDescription = TestUtil.generateRandomDescription();
        int memberCount = 2;
        int folderCount = 4;
        int environmentCount = TestUtil.generateRandomInt();
        WorkspaceResponse response = new WorkspaceResponse(workspaceId, updatedWorkspaceName, workspaceDescription,
                LocalDateTime.now(), memberCount, folderCount, environmentCount);

        when(workspaceService.updateWorkspace(eq(workspaceId), any(WorkspaceRequest.class), anyString()))
                .thenReturn(response);

        // when
        ResultActions res = mockMvc.perform(JsonTestUtil.putJsonAuth(
                WORKSPACES_URL + "/" + workspaceId, request, token
        ));

        // then
        res.andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpectAll(
                        jsonPath("$.id").value(workspaceId.intValue()),
                        jsonPath("$.name").value(updatedWorkspaceName),
                        jsonPath("$.description").value(workspaceDescription),
                        jsonPath("$.createdAt").exists(),
                        jsonPath("$.memberCount").value(memberCount),
                        jsonPath("$.folderCount").value(folderCount),
                        jsonPath("$.environmentCount").value(environmentCount)
                );

        verify(workspaceService).updateWorkspace(eq(workspaceId), any(WorkspaceRequest.class), anyString());
    }

    @Test
    void shouldReturnForbiddenWhenUpdateWorkspaceThrows() throws Exception {
        // given
        when(workspaceService.updateWorkspace(anyLong(), any(), anyString()))
                .thenThrow(new AccessDeniedException("forbidden"));

        // when / then
        mockMvc.perform(JsonTestUtil.putJsonAuth(
                        WORKSPACES_URL + "/" + TestUtil.generateRandomId(), new WorkspaceRequest("X"), token
                ))
                .andExpect(status().isForbidden());

        verify(workspaceService).updateWorkspace(anyLong(), any(), anyString());
    }

    @Test
    void shouldPatchWorkspace() throws Exception {
        // given
        WorkspaceRequest request = new WorkspaceRequest(TestUtil.generateRandomName());

        Long workspaceId = TestUtil.generateRandomId();
        String updatedWorkspaceName = TestUtil.generateRandomName();
        String workspaceDescription = TestUtil.generateRandomDescription();
        int memberCount = TestUtil.generateRandomInt();
        int folderCount = TestUtil.generateRandomInt();
        int environmentCount = TestUtil.generateRandomInt();
        WorkspaceResponse response = new WorkspaceResponse(workspaceId, updatedWorkspaceName, workspaceDescription,
                LocalDateTime.now(), memberCount, folderCount, environmentCount);

        when(workspaceService.patchWorkspace(eq(workspaceId), any(WorkspaceRequest.class), anyString()))
                .thenReturn(response);

        // when
        ResultActions res = mockMvc.perform(JsonTestUtil.patchJsonAuth(
                WORKSPACES_URL + "/" + workspaceId, request, token
        ));

        // then
        res.andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpectAll(
                        jsonPath("$.id").value(workspaceId.intValue()),
                        jsonPath("$.name").value(updatedWorkspaceName),
                        jsonPath("$.description").value(workspaceDescription),
                        jsonPath("$.createdAt").exists(),
                        jsonPath("$.memberCount").value(memberCount),
                        jsonPath("$.folderCount").value(folderCount),
                        jsonPath("$.environmentCount").value(environmentCount)
                );

        verify(workspaceService).patchWorkspace(eq(workspaceId), any(WorkspaceRequest.class), anyString());
    }

    @Test
    void shouldReturnForbiddenWhenPatchWorkspaceThrows() throws Exception {
        // given
        when(workspaceService.patchWorkspace(anyLong(), any(), anyString()))
                .thenThrow(new AccessDeniedException("forbidden"));

        // when / then
        mockMvc.perform(JsonTestUtil.patchJsonAuth(
                        WORKSPACES_URL + "/" + TestUtil.generateRandomId(), new WorkspaceRequest("X"), token
                ))
                .andExpect(status().isForbidden());

        verify(workspaceService).patchWorkspace(anyLong(), any(), anyString());
    }

    @Test
    void shouldDeleteWorkspace() throws Exception {
        // given
        doNothing().when(workspaceService).deleteWorkspace(anyLong(), anyString());

        // when / then
        mockMvc.perform(JsonTestUtil.deleteAuth(WORKSPACES_URL + "/" + TestUtil.generateRandomId(), token))
                .andExpect(status().isNoContent());

        verify(workspaceService).deleteWorkspace(anyLong(), anyString());
    }

    @Test
    void shouldReturnForbiddenWhenDeleteWorkspaceThrows() throws Exception {
        // given
        Long workspaceId = TestUtil.generateRandomId();
        doThrow(new AccessDeniedException("forbidden"))
                .when(workspaceService)
                .deleteWorkspace(eq(workspaceId), anyString());

        // when / then
        mockMvc.perform(JsonTestUtil.deleteAuth(WORKSPACES_URL + "/" + workspaceId, token))
                .andExpect(status().isForbidden());

        verify(workspaceService).deleteWorkspace(eq(workspaceId), anyString());
    }

    @Test
    void shouldAddMember() throws Exception {
        // given
        AddMemberRequest request = new AddMemberRequest(TestUtil.generateRandomEmail());

        Long workspaceId = TestUtil.generateRandomId();
        String workspaceName = TestUtil.generateRandomName();
        String workspaceDescription = TestUtil.generateRandomDescription();
        int memberCount = TestUtil.generateRandomInt();
        int folderCount = TestUtil.generateRandomInt();
        int environmentCount = TestUtil.generateRandomInt();
        WorkspaceResponse response = new WorkspaceResponse(workspaceId, workspaceName, workspaceDescription,
                LocalDateTime.now(), memberCount, folderCount, environmentCount);

        when(workspaceService.addMember(eq(workspaceId), any(AddMemberRequest.class), anyString()))
                .thenReturn(response);

        // when
        ResultActions res = mockMvc.perform(JsonTestUtil.postJsonAuth(
                WORKSPACES_URL + "/" + workspaceId + "/members", request, token
        ));

        res.andDo(print());

        // then
        res.andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpectAll(
                        jsonPath("$.id").value(workspaceId.intValue()),
                        jsonPath("$.name").value(workspaceName),
                        jsonPath("$.description").value(workspaceDescription),
                        jsonPath("$.createdAt").exists(),
                        jsonPath("$.memberCount").value(memberCount),
                        jsonPath("$.folderCount").value(folderCount),
                        jsonPath("$.environmentCount").value(environmentCount)
                );

        verify(workspaceService).addMember(eq(workspaceId), any(AddMemberRequest.class), anyString());
    }

    @Test
    void shouldReturnBadRequestWhenAddMemberThrows() throws Exception {
        // given
        AddMemberRequest request = new AddMemberRequest(TestUtil.generateRandomEmail());

        Long workspaceId = TestUtil.generateRandomId();
        when(workspaceService.addMember(eq(workspaceId), any(AddMemberRequest.class), anyString()))
                .thenThrow(new BadRequestException("bad request"));

        // when / then
        mockMvc.perform(JsonTestUtil.postJsonAuth(WORKSPACES_URL + "/" + workspaceId + "/members", request, token))
                .andExpect(status().isBadRequest());

        verify(workspaceService).addMember(eq(workspaceId), any(AddMemberRequest.class), anyString());
    }

    @Test
    void shouldRemoveMember() throws Exception {
        // given
        Long userId = TestUtil.generateRandomId();

        Long workspaceId = TestUtil.generateRandomId();
        String workspaceName = TestUtil.generateRandomName();
        String workspaceDescription = TestUtil.generateRandomDescription();
        int memberCount = 1;
        int folderCount = 1;
        int environmentCount = TestUtil.generateRandomInt();
        WorkspaceResponse response = new WorkspaceResponse(workspaceId, workspaceName, workspaceDescription,
                LocalDateTime.now(), memberCount, folderCount, environmentCount);

        when(workspaceService.removeMember(eq(workspaceId), eq(userId), anyString()))
                .thenReturn(response);

        // when
        ResultActions res = mockMvc.perform(JsonTestUtil.deleteAuth(
                WORKSPACES_URL + "/" + workspaceId + "/members/" + userId, token
        ));

        // then
        res.andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpectAll(
                        jsonPath("$.id").value(workspaceId.intValue()),
                        jsonPath("$.name").value(workspaceName),
                        jsonPath("$.description").value(workspaceDescription),
                        jsonPath("$.createdAt").exists(),
                        jsonPath("$.memberCount").value(memberCount),
                        jsonPath("$.folderCount").value(folderCount),
                        jsonPath("$.environmentCount").value(environmentCount)
                );

        verify(workspaceService).removeMember(eq(workspaceId), eq(userId), anyString());
    }

    @Test
    void shouldReturnBadRequestWhenRemoveMemberThrows() throws Exception {
        // given
        Long workspaceId = TestUtil.generateRandomId();
        Long userId = TestUtil.generateRandomId();
        when(workspaceService.removeMember(eq(workspaceId), eq(userId), anyString()))
                .thenThrow(new BadRequestException("bad request"));

        // when / then
        mockMvc.perform(JsonTestUtil.deleteAuth(WORKSPACES_URL + "/" + workspaceId + "/members/" + userId, token))
                .andExpect(status().isBadRequest());

        verify(workspaceService).removeMember(eq(workspaceId), eq(userId), anyString());
    }
}
