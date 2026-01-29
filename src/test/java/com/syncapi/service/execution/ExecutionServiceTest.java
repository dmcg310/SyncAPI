package com.syncapi.service.execution;

import com.syncapi.JsonTestUtil;
import com.syncapi.TestUtil;
import com.syncapi.dto.execution.ExecutionResponse;
import com.syncapi.dto.request.SubstitutedRequest;
import com.syncapi.entity.environment.Environment;
import com.syncapi.entity.folder.Folder;
import com.syncapi.entity.request.Request;
import com.syncapi.service.substitution.VariableSubstitutionService;
import com.syncapi.util.RequestMethod;
import com.syncapi.util.Util;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExecutionServiceTest {
    @Mock
    private VariableSubstitutionService variableSubstitutionService;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private Util util;

    @InjectMocks
    private ExecutionService executionService;

    private final Long requestId = TestUtil.generateRandomId();
    private final String email = TestUtil.generateRandomEmail();

    private Request request;
    private SubstitutedRequest substitutedRequest;

    @BeforeEach
    void setUp() {
        String name = TestUtil.generateRandomName();
        RequestMethod method = TestUtil.generateRandomRequestMethod();
        String url = TestUtil.generateRandomUrl();

        Folder folder = TestUtil.createRandomFolder(TestUtil.createRandomWorkspace());
        request = TestUtil.createRequest(TestUtil.generateRandomId(), name, method, url, folder);
        substitutedRequest = new SubstitutedRequest(url, JsonTestUtil.jsonContentTypeHeader(), null);
    }

    @Test
    void shouldExecuteGetRequestSuccessfully() {
        // given
        Map<String, String> responseBody = TestUtil.createRandomMap(2);
        ResponseEntity<Object> responseEntity = ResponseEntity.ok(responseBody);

        when(util.getActiveEnvironmentByRequestId(requestId, email)).thenReturn(Optional.empty());
        when(util.getRequestWithAccessCheck(requestId, email)).thenReturn(request);
        when(variableSubstitutionService.substituteRequest(anyString(), any(), any(), any()))
                .thenReturn(substitutedRequest);
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), eq(Object.class)))
                .thenReturn(responseEntity);

        // when
        ExecutionResponse response = executionService.execute(requestId, email);

        // then
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getStatusCode()).isEqualTo(JsonTestUtil.SUCCESS_STATUS_CODE);
        assertThat(response.getBody()).isEqualTo(responseBody);
        assertThat(response.getErrorMessage()).isNull();
        assertThat(response.getResponseTimeMs()).isGreaterThanOrEqualTo(0);
    }

    @Test
    void shouldExecutePostRequestWithBody() {
        // given
        String postUrl = TestUtil.generateRandomUrl();
        Map<String, Object> requestBody = Map.of(TestUtil.generateRandomKey(), TestUtil.generateRandomValue());

        Request postRequest = TestUtil.createRequest(TestUtil.generateRandomId(), TestUtil.generateRandomName(),
                RequestMethod.POST, postUrl, request.getFolder());
        postRequest.setBody(requestBody);

        SubstitutedRequest substitutedPostRequest = new SubstitutedRequest(
                postUrl,
                JsonTestUtil.jsonContentTypeHeader(),
                requestBody
        );

        Map<String, String> responseBody = TestUtil.createRandomMap(2);
        ResponseEntity<Object> responseEntity = ResponseEntity.status(HttpStatus.CREATED).body(responseBody);

        when(util.getActiveEnvironmentByRequestId(requestId, email)).thenReturn(Optional.empty());
        when(util.getRequestWithAccessCheck(requestId, email)).thenReturn(postRequest);
        when(variableSubstitutionService.substituteRequest(anyString(), any(), any(), any()))
                .thenReturn(substitutedPostRequest);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(Object.class)))
                .thenReturn(responseEntity);

        // when
        ExecutionResponse response = executionService.execute(requestId, email);

        // then
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED.value());
        assertThat(response.getBody()).isEqualTo(responseBody);
    }

    @Test
    void shouldExecuteRequestWithVariableSubstitution() {
        // given
        Environment environment = TestUtil.createRandomEnvironment(TestUtil.createRandomWorkspace());

        String baseUrlKey = TestUtil.generateRandomKey();
        String baseUrlValue = TestUtil.generateRandomUrl();
        String tokenKey = TestUtil.generateRandomKey();
        String tokenValue = TestUtil.generateRandomToken();
        Map<String, String> variables = Map.of(baseUrlKey, baseUrlValue, tokenKey, tokenValue);

        String substitutedUrl = baseUrlValue + "/users";
        SubstitutedRequest substitutedWithVars = new SubstitutedRequest(substitutedUrl,
                JsonTestUtil.authHeader(tokenValue), null);

        Map<String, String> responseBody = TestUtil.createRandomMap(1);
        ResponseEntity<Object> responseEntity = ResponseEntity.ok(responseBody);

        when(util.getActiveEnvironmentByRequestId(requestId, email)).thenReturn(Optional.of(environment));
        when(util.getVariablesFromEnvironment(environment)).thenReturn(variables);
        when(util.getRequestWithAccessCheck(requestId, email)).thenReturn(request);
        when(variableSubstitutionService.substituteRequest(anyString(), any(), any(), eq(variables)))
                .thenReturn(substitutedWithVars);
        when(restTemplate.exchange(eq(substitutedUrl), any(HttpMethod.class), any(HttpEntity.class), eq(Object.class)))
                .thenReturn(responseEntity);

        // when
        ExecutionResponse response = executionService.execute(requestId, email);

        // then
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getStatusCode()).isEqualTo(JsonTestUtil.SUCCESS_STATUS_CODE);
    }

    @Test
    void shouldHandleHttpClientError() {
        // given
        String errorMessage = TestUtil.generateRandomErrorMessage();

        when(util.getActiveEnvironmentByRequestId(requestId, email)).thenReturn(Optional.empty());
        when(util.getRequestWithAccessCheck(requestId, email)).thenReturn(request);
        when(variableSubstitutionService.substituteRequest(anyString(), any(), any(), any()))
                .thenReturn(substitutedRequest);
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), eq(Object.class)))
                .thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND, errorMessage));

        // when
        ExecutionResponse response = executionService.execute(requestId, email);

        // then
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND.value());
        assertThat(response.getErrorMessage()).isNull();
    }

    @Test
    void shouldHandleHttpServerError() {
        // given
        String errorMessage = TestUtil.generateRandomErrorMessage();

        when(util.getActiveEnvironmentByRequestId(requestId, email)).thenReturn(Optional.empty());
        when(util.getRequestWithAccessCheck(requestId, email)).thenReturn(request);
        when(variableSubstitutionService.substituteRequest(anyString(), any(), any(), any()))
                .thenReturn(substitutedRequest);
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), eq(Object.class)))
                .thenThrow(new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR, errorMessage));

        // when
        ExecutionResponse response = executionService.execute(requestId, email);

        // then
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
    }

    @Test
    void shouldHandleConnectionFailure() {
        // given
        String errorMessage = TestUtil.generateRandomErrorMessage();

        when(util.getActiveEnvironmentByRequestId(requestId, email)).thenReturn(Optional.empty());
        when(util.getRequestWithAccessCheck(requestId, email)).thenReturn(request);
        when(variableSubstitutionService.substituteRequest(anyString(), any(), any(), any()))
                .thenReturn(substitutedRequest);
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), eq(Object.class)))
                .thenThrow(new ResourceAccessException(errorMessage));

        // when
        ExecutionResponse response = executionService.execute(requestId, email);

        // then
        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getStatusCode()).isEqualTo(0);
        assertThat(response.getErrorMessage()).isEqualTo(errorMessage);
        assertThat(response.getHeaders()).isNull();
        assertThat(response.getBody()).isNull();
    }

    @Test
    void shouldHandleTimeout() {
        // given
        String timeoutMessage = "Read timed out";

        when(util.getActiveEnvironmentByRequestId(requestId, email)).thenReturn(Optional.empty());
        when(util.getRequestWithAccessCheck(requestId, email)).thenReturn(request);
        when(variableSubstitutionService.substituteRequest(anyString(), any(), any(), any()))
                .thenReturn(substitutedRequest);
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), eq(Object.class)))
                .thenThrow(new ResourceAccessException(timeoutMessage));

        // when
        ExecutionResponse response = executionService.execute(requestId, email);

        // then
        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getErrorMessage()).contains("timed out");
    }
}
