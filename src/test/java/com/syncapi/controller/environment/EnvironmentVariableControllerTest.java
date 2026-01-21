package com.syncapi.controller.environment;

import com.syncapi.JsonTestUtil;
import com.syncapi.TestUtil;
import com.syncapi.dto.environment.EnvironmentVariableRequest;
import com.syncapi.dto.environment.EnvironmentVariableResponse;
import com.syncapi.security.jwt.JwtService;
import com.syncapi.service.environment.EnvironmentVariableService;
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

import java.util.List;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = EnvironmentVariableController.class)
@AutoConfigureMockMvc(addFilters = false)
class EnvironmentVariableControllerTest {
    private static final String VAR_URL = "/api/environments/{environmentId}/variables";
    private static final String ENV_ID_PLACEHOLDER = "{environmentId}";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EnvironmentVariableService environmentVariableService;

    @MockitoBean
    private JwtService jwtService;

    private String token;
    private Long environmentId;

    @BeforeEach
    void setUp() {
        reset(environmentVariableService);

        token = TestUtil.generateRandomToken();
        environmentId = TestUtil.generateRandomId();

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(TestUtil.generateRandomEmail(), null, List.of())
        );
    }

    @Test
    void shouldGetVariablesByEnvironment() throws Exception {
        // given
        EnvironmentVariableResponse var1 = createVarResponse();
        EnvironmentVariableResponse var2 = createVarResponse();

        when(environmentVariableService.getVariablesByEnvironment(eq(environmentId), anyString()))
                .thenReturn(List.of(var1, var2));

        // when
        ResultActions res = mockMvc.perform(JsonTestUtil.getJsonAuth(
                VAR_URL.replace(ENV_ID_PLACEHOLDER, environmentId.toString()), token
        ));

        // then
        res.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(2));

        verify(environmentVariableService).getVariablesByEnvironment(eq(environmentId), anyString());
    }

    @Test
    void shouldReturnForbiddenWhenGetVariablesThrows() throws Exception {
        // given
        when(environmentVariableService.getVariablesByEnvironment(eq(environmentId), anyString()))
                .thenThrow(new RuntimeException("access denied"));

        // when / then
        mockMvc.perform(JsonTestUtil.getJsonAuth(
                        VAR_URL.replace(ENV_ID_PLACEHOLDER, environmentId.toString()), token
                ))
                .andExpect(status().isForbidden());

        verify(environmentVariableService).getVariablesByEnvironment(eq(environmentId), anyString());
    }

    @Test
    void shouldAddVariable() throws Exception {
        // given
        EnvironmentVariableRequest request = new EnvironmentVariableRequest(TestUtil.generateRandomKey(),
                TestUtil.generateRandomValue());

        EnvironmentVariableResponse response = new EnvironmentVariableResponse(TestUtil.generateRandomId(),
                request.getKey(), request.getValue(), environmentId);

        when(environmentVariableService.addVariable(eq(environmentId), any(EnvironmentVariableRequest.class), anyString()))
                .thenReturn(response);

        // when
        ResultActions res = mockMvc.perform(JsonTestUtil.postJsonAuth(
                VAR_URL.replace(ENV_ID_PLACEHOLDER, environmentId.toString()), request, token
        ));

        // then
        res.andExpect(status().isCreated())
                .andExpectAll(
                        jsonPath("$.id").value(response.getId()),
                        jsonPath("$.key").value(request.getKey()),
                        jsonPath("$.value").value(request.getValue()),
                        jsonPath("$.environmentId").value(environmentId)
                );

        verify(environmentVariableService).addVariable(eq(environmentId), any(EnvironmentVariableRequest.class), anyString());
    }

    @Test
    void shouldReturnForbiddenWhenAddVariableThrows() throws Exception {
        // given
        EnvironmentVariableRequest request = new EnvironmentVariableRequest(
                TestUtil.generateRandomKey(),
                TestUtil.generateRandomValue()
        );

        when(environmentVariableService.addVariable(eq(environmentId), any(EnvironmentVariableRequest.class), anyString()))
                .thenThrow(new RuntimeException("access denied"));

        // when / then
        mockMvc.perform(JsonTestUtil.postJsonAuth(
                        VAR_URL.replace(ENV_ID_PLACEHOLDER, environmentId.toString()), request, token
                ))
                .andExpect(status().isForbidden());

        verify(environmentVariableService).addVariable(eq(environmentId), any(EnvironmentVariableRequest.class), anyString());
    }

    @ParameterizedTest
    @MethodSource("invalidVariableRequests")
    void shouldFailValidationForInvalidCreate(EnvironmentVariableRequest request) throws Exception {
        // when / then
        mockMvc.perform(JsonTestUtil.postJsonAuth(
                        VAR_URL.replace(ENV_ID_PLACEHOLDER, environmentId.toString()), request, token
                ))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(environmentVariableService);
    }

    @Test
    void shouldUpdateVariable() throws Exception {
        // given
        Long variableId = TestUtil.generateRandomId();

        EnvironmentVariableRequest request = new EnvironmentVariableRequest(TestUtil.generateRandomKey(),
                TestUtil.generateRandomValue());

        EnvironmentVariableResponse response = new EnvironmentVariableResponse(variableId, request.getKey(),
                request.getValue(), environmentId);

        when(environmentVariableService.updateVariable(eq(environmentId), eq(variableId), any(EnvironmentVariableRequest.class), anyString()))
                .thenReturn(response);

        // when
        ResultActions res = mockMvc.perform(JsonTestUtil.putJsonAuth(
                VAR_URL.replace(ENV_ID_PLACEHOLDER, environmentId.toString()) + "/" + variableId, request, token
        ));

        // then
        res.andExpect(status().isOk())
                .andExpectAll(
                        jsonPath("$.id").value(variableId),
                        jsonPath("$.key").value(request.getKey()),
                        jsonPath("$.value").value(request.getValue())
                );

        verify(environmentVariableService)
                .updateVariable(eq(environmentId), eq(variableId), any(EnvironmentVariableRequest.class), anyString());
    }

    @Test
    void shouldReturnForbiddenWhenUpdateVariableThrows() throws Exception {
        // given
        Long variableId = TestUtil.generateRandomId();
        EnvironmentVariableRequest request = new EnvironmentVariableRequest(
                TestUtil.generateRandomKey(),
                TestUtil.generateRandomValue()
        );

        when(environmentVariableService.updateVariable(eq(environmentId), eq(variableId),
                any(EnvironmentVariableRequest.class), anyString())).thenThrow(new RuntimeException("access denied"));

        // when / then
        mockMvc.perform(JsonTestUtil.putJsonAuth(
                        VAR_URL.replace(ENV_ID_PLACEHOLDER, environmentId.toString()) + "/" + variableId,
                        request,
                        token
                ))
                .andExpect(status().isForbidden());

        verify(environmentVariableService).updateVariable(eq(environmentId), eq(variableId),
                any(EnvironmentVariableRequest.class), anyString());
    }

    @ParameterizedTest
    @MethodSource("invalidVariableRequests")
    void shouldFailValidationForInvalidUpdate(EnvironmentVariableRequest request) throws Exception {
        // given
        Long variableId = TestUtil.generateRandomId();

        // when / then
        mockMvc.perform(JsonTestUtil.putJsonAuth(
                        VAR_URL.replace(ENV_ID_PLACEHOLDER, environmentId.toString()) + "/" + variableId, request, token
                ))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(environmentVariableService);
    }

    @Test
    void shouldDeleteVariable() throws Exception {
        // given
        Long variableId = TestUtil.generateRandomId();
        doNothing().when(environmentVariableService).deleteVariable(eq(environmentId), eq(variableId), anyString());

        // when / then
        mockMvc.perform(JsonTestUtil.deleteAuth(
                        VAR_URL.replace(ENV_ID_PLACEHOLDER, environmentId.toString()) + "/" + variableId, token
                ))
                .andExpect(status().isNoContent());

        verify(environmentVariableService).deleteVariable(eq(environmentId), eq(variableId), anyString());
    }

    @Test
    void shouldReturnForbiddenWhenDeleteVariableThrows() throws Exception {
        // given
        Long variableId = TestUtil.generateRandomId();

        doThrow(new RuntimeException("access denied"))
                .when(environmentVariableService)
                .deleteVariable(eq(environmentId), eq(variableId), anyString());

        // when / then
        mockMvc.perform(JsonTestUtil.deleteAuth(
                        V.replace(ENV_ID_PLACEHOLDER, environmentId.toString()) + "/" + variableId,
                        token
                ))
                .andExpect(status().isForbidden());

        verify(environmentVariableService).deleteVariable(eq(environmentId), eq(variableId), anyString());
    }

    private static Stream<EnvironmentVariableRequest> invalidVariableRequests() {
        return Stream.of(
                new EnvironmentVariableRequest(null, "v"),
                new EnvironmentVariableRequest("", "v"),
                new EnvironmentVariableRequest("   ", "v"),
                new EnvironmentVariableRequest("k", null),
                new EnvironmentVariableRequest("k", ""),
                new EnvironmentVariableRequest("k", "   ")
        );
    }

    private EnvironmentVariableResponse createVarResponse() {
        return new EnvironmentVariableResponse(
                TestUtil.generateRandomId(),
                TestUtil.generateRandomKey(),
                TestUtil.generateRandomValue(),
                environmentId
        );
    }
}
