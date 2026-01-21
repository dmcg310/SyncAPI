package com.syncapi.controller.environment;

import com.syncapi.JsonTestUtil;
import com.syncapi.TestUtil;
import com.syncapi.dto.environment.EnvironmentRequest;
import com.syncapi.dto.environment.EnvironmentResponse;
import com.syncapi.dto.environment.EnvironmentVariableResponse;
import com.syncapi.security.jwt.JwtService;
import com.syncapi.service.environment.EnvironmentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
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
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = EnvironmentController.class)
@AutoConfigureMockMvc(addFilters = false)
class EnvironmentControllerTest {
    private static final String ENV_URL = "/api/workspaces/{workspaceId}/environments";
    private static final String WORKSPACE_ID_PLACEHOLDER = "{workspaceId}";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EnvironmentService environmentService;

    @MockitoBean
    private JwtService jwtService;

    private String token;
    private Long workspaceId;

    @BeforeEach
    void setUp() {
        reset(environmentService);

        token = TestUtil.generateRandomToken();
        workspaceId = TestUtil.generateRandomId();

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(TestUtil.generateRandomEmail(), null, List.of())
        );
    }

    @Test
    void shouldGetEnvironmentsByWorkspace() throws Exception {
        // given
        EnvironmentResponse environment1 = createEnvironmentResponse();
        EnvironmentResponse environment2 = createEnvironmentResponse();

        when(environmentService.getEnvironmentsByWorkspace(eq(workspaceId), anyString()))
                .thenReturn(List.of(environment1, environment2));

        // when
        ResultActions res = mockMvc.perform(JsonTestUtil.getJsonAuth(
                ENV_URL.replace(WORKSPACE_ID_PLACEHOLDER, workspaceId.toString()), token
        ));

        // then
        res.andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpectAll(
                        jsonPath("$[0].id").value(environment1.getId()),
                        jsonPath("$[1].id").value(environment2.getId())
                );

        verify(environmentService).getEnvironmentsByWorkspace(eq(workspaceId), anyString());
    }

    @Test
    void shouldReturnForbiddenWhenGetEnvironmentsThrows() throws Exception {
        // given
        when(environmentService.getEnvironmentsByWorkspace(eq(workspaceId), anyString()))
                .thenThrow(new RuntimeException("access denied"));

        // when / then
        mockMvc.perform(JsonTestUtil.getJsonAuth(
                        ENV_URL.replace(WORKSPACE_ID_PLACEHOLDER, workspaceId.toString()), token
                ))
                .andExpect(status().isForbidden());

        verify(environmentService).getEnvironmentsByWorkspace(eq(workspaceId), anyString());
    }

    @Test
    void shouldGetEnvironmentById_includesVariables() throws Exception {
        // given
        Long environmentId = TestUtil.generateRandomId();

        EnvironmentResponse env = createEnvironmentResponse(environmentId);
        env.setVariableCount(2);
        env.setVariables(List.of(
                new EnvironmentVariableResponse(TestUtil.generateRandomId(), TestUtil.generateRandomKey(),
                        TestUtil.generateRandomValue(), environmentId),
                new EnvironmentVariableResponse(TestUtil.generateRandomId(), TestUtil.generateRandomKey(),
                        TestUtil.generateRandomValue(), environmentId)
        ));

        when(environmentService.getEnvironmentById(eq(environmentId), anyString()))
                .thenReturn(env);

        // when
        ResultActions res = mockMvc.perform(JsonTestUtil.getJsonAuth(
                ENV_URL.replace(WORKSPACE_ID_PLACEHOLDER, workspaceId.toString()) + "/" + environmentId, token
        ));

        // then
        res.andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpectAll(
                        jsonPath("$.id").value(environmentId),
                        jsonPath("$.variableCount").value(2),
                        jsonPath("$.variables.length()").value(2),
                        jsonPath("$.variables[0].environmentId").value(environmentId)
                );

        verify(environmentService).getEnvironmentById(eq(environmentId), anyString());
    }

    @Test
    void shouldCreateEnvironment() throws Exception {
        // given
        EnvironmentRequest request = new EnvironmentRequest();
        request.setName(TestUtil.generateRandomName());
        request.setDescription(TestUtil.generateRandomDescription());
        request.setIsActive(true);

        EnvironmentResponse response = createEnvironmentResponse(TestUtil.generateRandomId());
        response.setName(request.getName());
        response.setDescription(request.getDescription());
        response.setIsActive(true);

        when(environmentService.createEnvironment(eq(workspaceId), any(EnvironmentRequest.class), anyString()))
                .thenReturn(response);

        // when
        ResultActions res = mockMvc.perform(JsonTestUtil.postJsonAuth(
                ENV_URL.replace(WORKSPACE_ID_PLACEHOLDER, workspaceId.toString()), request, token
        ));

        // then
        res.andExpect(status().isCreated())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpectAll(
                        jsonPath("$.id").value(response.getId()),
                        jsonPath("$.name").value(request.getName()),
                        jsonPath("$.description").value(request.getDescription()),
                        jsonPath("$.isActive").value(true)
                );

        verify(environmentService).createEnvironment(eq(workspaceId), any(EnvironmentRequest.class), anyString());
    }

    @ParameterizedTest
    @MethodSource("invalidEnvironmentRequests")
    void shouldFailValidationForInvalidCreateEnvironment(EnvironmentRequest request) throws Exception {
        // when / then
        mockMvc.perform(JsonTestUtil.postJsonAuth(
                        ENV_URL.replace(WORKSPACE_ID_PLACEHOLDER, workspaceId.toString()), request, token
                ))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(environmentService);
    }

    @Test
    void shouldUpdateEnvironment() throws Exception {
        // given
        Long environmentId = TestUtil.generateRandomId();

        EnvironmentRequest request = new EnvironmentRequest();
        request.setName(TestUtil.generateRandomName());
        request.setDescription(TestUtil.generateRandomDescription());
        request.setIsActive(false);

        EnvironmentResponse response = createEnvironmentResponse(environmentId);
        response.setName(request.getName());
        response.setDescription(request.getDescription());
        response.setIsActive(false);

        when(environmentService.updateEnvironment(eq(environmentId), any(EnvironmentRequest.class), anyString()))
                .thenReturn(response);

        // when
        ResultActions res = mockMvc.perform(JsonTestUtil.putJsonAuth(
                ENV_URL.replace(WORKSPACE_ID_PLACEHOLDER, workspaceId.toString()) + "/" + environmentId, request,
                token
        ));

        // then
        res.andExpect(status().isOk())
                .andExpectAll(
                        jsonPath("$.id").value(environmentId),
                        jsonPath("$.name").value(request.getName()),
                        jsonPath("$.isActive").value(false)
                );

        verify(environmentService).updateEnvironment(eq(environmentId), any(EnvironmentRequest.class), anyString());
    }

    @ParameterizedTest
    @MethodSource("invalidEnvironmentRequests")
    void shouldFailValidationForInvalidUpdateEnvironment(EnvironmentRequest request) throws Exception {
        // given
        Long environmentId = TestUtil.generateRandomId();

        // when / then
        mockMvc.perform(JsonTestUtil.putJsonAuth(
                        ENV_URL.replace(WORKSPACE_ID_PLACEHOLDER, workspaceId.toString()) + "/" + environmentId,
                        request, token
                ))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(environmentService);
    }

    @Test
    void shouldPatchEnvironment() throws Exception {
        // given
        String description = TestUtil.generateRandomDescription();

        EnvironmentRequest request = new EnvironmentRequest();
        request.setDescription(description);

        Long environmentId = TestUtil.generateRandomId();
        EnvironmentResponse response = createEnvironmentResponse(environmentId);
        response.setDescription(description);

        when(environmentService.patchEnvironment(eq(environmentId), any(EnvironmentRequest.class), anyString()))
                .thenReturn(response);

        // when
        ResultActions res = mockMvc.perform(JsonTestUtil.patchJsonAuth(
                ENV_URL.replace(WORKSPACE_ID_PLACEHOLDER, workspaceId.toString()) + "/" + environmentId, request,
                token
        ));

        // then
        res.andExpect(status().isOk())
                .andExpectAll(
                        jsonPath("$.id").value(environmentId),
                        jsonPath("$.description").value(description)
                );

        verify(environmentService).patchEnvironment(eq(environmentId), any(EnvironmentRequest.class), anyString());
    }

    @Test
    void shouldDeleteEnvironment() throws Exception {
        // given
        Long environmentId = TestUtil.generateRandomId();
        doNothing().when(environmentService).deleteEnvironment(eq(environmentId), anyString());

        // when / then
        mockMvc.perform(JsonTestUtil.deleteAuth(
                        ENV_URL.replace(WORKSPACE_ID_PLACEHOLDER, workspaceId.toString()) + "/" + environmentId,
                        token
                ))
                .andExpect(status().isNoContent());

        verify(environmentService).deleteEnvironment(eq(environmentId), anyString());
    }

    @Test
    void shouldActivateEnvironment() throws Exception {
        // given
        Long environmentId = TestUtil.generateRandomId();

        EnvironmentResponse response = createEnvironmentResponse(environmentId);
        response.setIsActive(true);

        when(environmentService.setEnvironmentActiveStatus(eq(environmentId), eq(true), anyString()))
                .thenReturn(response);

        // when
        ResultActions res =
                mockMvc.perform(patch(ENV_URL.replace(WORKSPACE_ID_PLACEHOLDER, workspaceId.toString())
                        + "/" + environmentId
                        + "/activate")
                        .header(JsonTestUtil.AUTH_HEADER, JsonTestUtil.BEARER_PREFIX + token)
                        .accept(APPLICATION_JSON)
                );

        // then
        res.andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(environmentId))
                .andExpect(jsonPath("$.isActive").value(true));

        verify(environmentService).setEnvironmentActiveStatus(eq(environmentId), eq(true), anyString());
    }

    private static Stream<EnvironmentRequest> invalidEnvironmentRequests() {
        EnvironmentRequest nullName = new EnvironmentRequest();
        nullName.setName(null);

        EnvironmentRequest emptyName = new EnvironmentRequest();
        emptyName.setName("");

        EnvironmentRequest blankName = new EnvironmentRequest();
        blankName.setName("   ");

        return Stream.of(nullName, emptyName, blankName);
    }

    private EnvironmentResponse createEnvironmentResponse() {
        return createEnvironmentResponse(TestUtil.generateRandomId());
    }

    private EnvironmentResponse createEnvironmentResponse(Long environmentId) {
        return new EnvironmentResponse(
                environmentId,
                TestUtil.generateRandomName(),
                TestUtil.generateRandomDescription(),
                false,
                LocalDateTime.now(),
                workspaceId,
                TestUtil.generateRandomInt()
        );
    }
}
