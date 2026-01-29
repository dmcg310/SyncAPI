package com.syncapi.controller.execution;

import com.syncapi.JsonTestUtil;
import com.syncapi.TestUtil;
import com.syncapi.dto.execution.ExecutionResponse;
import com.syncapi.exception.ResourceNotFoundException;
import com.syncapi.security.jwt.JwtService;
import com.syncapi.service.execution.ExecutionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ExecutionController.class)
@AutoConfigureMockMvc(addFilters = false)
class ExecutionControllerTest {
    private static final String EXECUTE_URL = "/api/folders/{folderId}/requests/{requestId}/execute";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ExecutionService executionService;

    @MockitoBean
    private JwtService jwtService;

    private Long folderId;
    private Long requestId;

    @BeforeEach
    void setUp() {
        folderId = TestUtil.generateRandomId();
        requestId = TestUtil.generateRandomId();

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(TestUtil.generateRandomEmail(), null, List.of())
        );
    }

    @Test
    void shouldExecuteRequestSuccessfully() throws Exception {
        // given
        Map<String, String> responseBody = TestUtil.createRandomMap(2);
        long responseTimeMs = TestUtil.generateRandomLong();
        boolean success = true;
        ExecutionResponse response = createExecutionResponse(JsonTestUtil.SUCCESS_STATUS_CODE,
                JsonTestUtil.SUCCESS_STATUS_TEXT, JsonTestUtil.jsonContentTypeHeader(), responseBody, responseTimeMs,
                success, null);

        when(executionService.execute(anyLong(), anyString())).thenReturn(response);

        // when
        ResultActions res = mockMvc.perform(JsonTestUtil.postJsonNoBody(
                EXECUTE_URL.replace("{folderId}", folderId.toString())
                        .replace("{requestId}", requestId.toString())
        ));

        // then
        res.andDo(print());
        res.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpectAll(
                        jsonPath("$.statusCode").value(JsonTestUtil.SUCCESS_STATUS_CODE),
                        jsonPath("$.success").value(success),
                        jsonPath("$.responseTimeMs").value(responseTimeMs),
                        jsonPath("$.body").isMap(),
                        jsonPath("$.body").value(responseBody),
                        jsonPath("$.body.length()").value(responseBody.size()),
                        jsonPath("$.errorMessage").doesNotExist()
                );

        verify(executionService).execute(anyLong(), anyString());
    }

    @Test
    void shouldReturnFailureResponseOnConnectionError() throws Exception {
        // given
        String errorMessage = TestUtil.generateRandomErrorMessage();
        boolean success = false;
        ExecutionResponse response = createExecutionResponse(JsonTestUtil.BAD_STATUS_CODE, null, null, null,
                TestUtil.generateRandomLong(), false, errorMessage);

        when(executionService.execute(anyLong(), anyString())).thenReturn(response);

        // when
        ResultActions res = mockMvc.perform(JsonTestUtil.postJsonNoBody(
                EXECUTE_URL.replace("{folderId}", folderId.toString())
                        .replace("{requestId}", requestId.toString())
        ));

        // then
        res.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpectAll(
                        jsonPath("$.statusCode").value(JsonTestUtil.BAD_STATUS_CODE),
                        jsonPath("$.success").value(success),
                        jsonPath("$.errorMessage").value(errorMessage)
                );

        verify(executionService).execute(anyLong(), anyString());
    }

    @Test
    void shouldReturnNotFoundWhenRequestDoesNotExist() throws Exception {
        // given
        when(executionService.execute(anyLong(), anyString()))
                .thenThrow(new ResourceNotFoundException("Request not found: " + requestId));

        // when / then
        mockMvc.perform(JsonTestUtil.postJsonNoBody(
                        EXECUTE_URL.replace("{folderId}", folderId.toString())
                                .replace("{requestId}", requestId.toString())
                ))
                .andExpect(status().isNotFound());

        verify(executionService).execute(anyLong(), anyString());
    }

    private ExecutionResponse createExecutionResponse(int statusCode, String statusText, Map<String, String> headers,
                                                      Object body, long responseTimeMs, boolean success,
                                                      String errorMessage) {
        return new ExecutionResponse(statusCode, statusText, headers, body, responseTimeMs, success, errorMessage);
    }
}
