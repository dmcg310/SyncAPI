package com.syncapi.controller.request;

import com.syncapi.JsonTestUtil;
import com.syncapi.TestUtil;
import com.syncapi.dto.request.RequestRequest;
import com.syncapi.dto.request.RequestResponse;
import com.syncapi.security.jwt.JwtService;
import com.syncapi.service.request.RequestService;
import com.syncapi.util.RequestMethod;
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

@WebMvcTest(controllers = RequestController.class)
@AutoConfigureMockMvc(addFilters = false)
class RequestControllerTest {
    private static final String REQUESTS_URL = "/api/folders/{folderId}/requests";
    private static final String FOLDER_ID_PLACEHOLDER = "{folderId}";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RequestService requestService;

    @MockitoBean
    private JwtService jwtService;

    private String token;
    private Long folderId;

    @BeforeEach
    void setUp() {
        reset(requestService);

        token = TestUtil.generateRandomToken();
        folderId = TestUtil.generateRandomId();

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(TestUtil.generateRandomEmail(), null, List.of())
        );
    }

    @Test
    void shouldGetRequestsByFolder() throws Exception {
        // given
        RequestResponse request1 = createRequestResponse();
        RequestResponse request2 = createRequestResponse();

        when(requestService.getRequestsByFolder(eq(folderId), anyString()))
                .thenReturn(List.of(request1, request2));

        // when
        ResultActions res = mockMvc.perform(JsonTestUtil.getJsonAuth(
                REQUESTS_URL.replace(FOLDER_ID_PLACEHOLDER, folderId.toString()), token
        ));

        // then
        res.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpectAll(
                        jsonPath("$[0].id").value(request1.getId()),
                        jsonPath("$[0].name").value(request1.getName()),
                        jsonPath("$[0].method").value(request1.getMethod().name()),
                        jsonPath("$[1].id").value(request2.getId()),
                        jsonPath("$[1].name").value(request2.getName())
                );

        verify(requestService).getRequestsByFolder(eq(folderId), anyString());
    }

    @Test
    void shouldReturnEmptyListWhenNoRequests() throws Exception {
        // given
        when(requestService.getRequestsByFolder(eq(folderId), anyString()))
                .thenReturn(List.of());

        // when
        ResultActions res = mockMvc.perform(JsonTestUtil.getJsonAuth(
                REQUESTS_URL.replace(FOLDER_ID_PLACEHOLDER, folderId.toString()), token
        ));

        // then
        res.andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void shouldReturnForbiddenWhenGetRequestsThrows() throws Exception {
        // given
        when(requestService.getRequestsByFolder(eq(folderId), anyString()))
                .thenThrow(new RuntimeException("access denied"));

        // when / then
        mockMvc.perform(JsonTestUtil.getJsonAuth(
                        REQUESTS_URL.replace(FOLDER_ID_PLACEHOLDER, folderId.toString()), token
                ))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldGetRequestById() throws Exception {
        // given
        Long requestId = TestUtil.generateRandomId();
        RequestResponse request = createRequestResponse(requestId);

        when(requestService.getRequestById(eq(requestId), anyString()))
                .thenReturn(request);

        // when
        ResultActions res = mockMvc.perform(JsonTestUtil.getJsonAuth(
                REQUESTS_URL.replace(FOLDER_ID_PLACEHOLDER, folderId.toString()) + "/" + requestId, token
        ));

        // then
        res.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpectAll(
                        jsonPath("$.id").value(requestId),
                        jsonPath("$.name").value(request.getName()),
                        jsonPath("$.description").value(request.getDescription()),
                        jsonPath("$.method").value(request.getMethod().name()),
                        jsonPath("$.url").value(request.getUrl()),
                        jsonPath("$.folderId").value(request.getFolderId())
                );

        verify(requestService).getRequestById(eq(requestId), anyString());
    }

    @Test
    void shouldReturnForbiddenWhenGetRequestByIdThrows() throws Exception {
        // given
        Long requestId = TestUtil.generateRandomId();

        when(requestService.getRequestById(eq(requestId), anyString()))
                .thenThrow(new RuntimeException("access denied"));

        // when / then
        mockMvc.perform(JsonTestUtil.getJsonAuth(
                        REQUESTS_URL.replace(FOLDER_ID_PLACEHOLDER, folderId.toString()) + "/" + requestId, token
                ))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldCreateRequest() throws Exception {
        // given
        String requestName = TestUtil.generateRandomName();
        String url = TestUtil.generateRandomUrl();
        RequestRequest request = new RequestRequest(requestName, RequestMethod.POST, url);
        request.setDescription(TestUtil.generateRandomDescription());
        request.setHeaders(JsonTestUtil.jsonContentTypeHeader());

        Long requestId = TestUtil.generateRandomId();
        RequestResponse response = new RequestResponse(requestId, requestName, TestUtil.generateRandomDescription(),
                RequestMethod.POST, url, JsonTestUtil.jsonContentTypeHeader(), null, null, null,
                null, null, LocalDateTime.now(), folderId);

        when(requestService.createRequest(eq(folderId), any(RequestRequest.class), anyString()))
                .thenReturn(response);

        // when
        ResultActions res = mockMvc.perform(JsonTestUtil.postJsonAuth(
                REQUESTS_URL.replace(FOLDER_ID_PLACEHOLDER, folderId.toString()), request, token
        ));

        // then
        res.andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpectAll(
                        jsonPath("$.id").value(requestId),
                        jsonPath("$.name").value(requestName),
                        jsonPath("$.method").value("POST"),
                        jsonPath("$.url").value(url),
                        jsonPath("$.folderId").value(folderId)
                );

        verify(requestService).createRequest(eq(folderId), any(RequestRequest.class), anyString());
    }

    @ParameterizedTest
    @MethodSource("invalidRequestRequests")
    void shouldFailValidationForInvalidCreateRequest(RequestRequest request) throws Exception {
        // when / then
        mockMvc.perform(JsonTestUtil.postJsonAuth(
                        REQUESTS_URL.replace(FOLDER_ID_PLACEHOLDER, folderId.toString()), request, token
                ))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(requestService);
    }

    private static Stream<RequestRequest> invalidRequestRequests() {
        String url = TestUtil.generateRandomUrl();

        return Stream.of(
                new RequestRequest(null, RequestMethod.GET, url),
                new RequestRequest("", RequestMethod.GET, url),
                new RequestRequest("   ", RequestMethod.GET, url),
                new RequestRequest("Test", null, url),
                new RequestRequest("Test", RequestMethod.GET, null),
                new RequestRequest("Test", RequestMethod.GET, ""),
                new RequestRequest("Test", RequestMethod.GET, "   ")
        );
    }

    @Test
    void shouldReturnForbiddenWhenCreateRequestThrows() throws Exception {
        // given
        RequestRequest request = new RequestRequest(TestUtil.generateRandomName(), RequestMethod.GET,
                TestUtil.generateRandomUrl());

        when(requestService.createRequest(eq(folderId), any(RequestRequest.class), anyString()))
                .thenThrow(new RuntimeException("access denied"));

        // when / then
        mockMvc.perform(JsonTestUtil.postJsonAuth(
                        REQUESTS_URL.replace(FOLDER_ID_PLACEHOLDER, folderId.toString()), request, token
                ))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldUpdateRequest() throws Exception {
        // given
        Long requestId = TestUtil.generateRandomId();
        String newName = TestUtil.generateRandomName();
        String newUrl = TestUtil.generateRandomUrl();
        RequestRequest request = new RequestRequest(newName, RequestMethod.PUT, newUrl);

        RequestResponse response = new RequestResponse(requestId, newName, null, RequestMethod.PUT, newUrl,
                null, null, null, null, null, null, LocalDateTime.now(),
                folderId);

        when(requestService.updateRequest(eq(requestId), any(RequestRequest.class), anyString()))
                .thenReturn(response);

        // when
        ResultActions res = mockMvc.perform(JsonTestUtil.putJsonAuth(
                REQUESTS_URL.replace(FOLDER_ID_PLACEHOLDER, folderId.toString()) + "/" + requestId, request, token
        ));

        // then
        res.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpectAll(
                        jsonPath("$.id").value(requestId),
                        jsonPath("$.name").value(newName),
                        jsonPath("$.method").value("PUT"),
                        jsonPath("$.url").value(newUrl)
                );

        verify(requestService).updateRequest(eq(requestId), any(RequestRequest.class), anyString());
    }

    @ParameterizedTest
    @MethodSource("invalidRequestRequests")
    void shouldFailValidationForInvalidUpdateRequest(RequestRequest request) throws Exception {
        // given
        Long requestId = TestUtil.generateRandomId();

        // when / then
        mockMvc.perform(JsonTestUtil.putJsonAuth(
                        REQUESTS_URL.replace(FOLDER_ID_PLACEHOLDER, folderId.toString()) + "/" + requestId, request, token
                ))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(requestService);
    }

    @Test
    void shouldReturnForbiddenWhenUpdateRequestThrows() throws Exception {
        // given
        Long requestId = TestUtil.generateRandomId();
        RequestRequest request = new RequestRequest(TestUtil.generateRandomName(), RequestMethod.GET,
                TestUtil.generateRandomUrl());

        when(requestService.updateRequest(eq(requestId), any(RequestRequest.class), anyString()))
                .thenThrow(new RuntimeException("access denied"));

        // when / then
        mockMvc.perform(JsonTestUtil.putJsonAuth(
                        REQUESTS_URL.replace(FOLDER_ID_PLACEHOLDER, folderId.toString()) + "/" + requestId, request, token
                ))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldPatchRequest() throws Exception {
        // given
        Long requestId = TestUtil.generateRandomId();
        String newName = TestUtil.generateRandomName();
        RequestRequest request = new RequestRequest();
        request.setName(newName);

        RequestResponse response = createRequestResponse(requestId);

        when(requestService.patchRequest(eq(requestId), any(RequestRequest.class), anyString()))
                .thenReturn(response);

        // when
        ResultActions res = mockMvc.perform(JsonTestUtil.patchJsonAuth(
                REQUESTS_URL.replace(FOLDER_ID_PLACEHOLDER, folderId.toString()) + "/" + requestId, request, token
        ));

        // then
        res.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(requestId));

        verify(requestService).patchRequest(eq(requestId), any(RequestRequest.class), anyString());
    }

    @Test
    void shouldReturnForbiddenWhenPatchRequestThrows() throws Exception {
        // given
        Long requestId = TestUtil.generateRandomId();
        RequestRequest request = new RequestRequest();
        request.setName(TestUtil.generateRandomName());

        when(requestService.patchRequest(eq(requestId), any(RequestRequest.class), anyString()))
                .thenThrow(new RuntimeException("access denied"));

        // when / then
        mockMvc.perform(JsonTestUtil.patchJsonAuth(
                        REQUESTS_URL.replace(FOLDER_ID_PLACEHOLDER, folderId.toString()) + "/" + requestId, request, token
                ))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldDeleteRequest() throws Exception {
        // given
        Long requestId = TestUtil.generateRandomId();

        doNothing().when(requestService).deleteRequest(eq(requestId), anyString());

        // when / then
        mockMvc.perform(JsonTestUtil.deleteAuth(
                        REQUESTS_URL.replace(FOLDER_ID_PLACEHOLDER, folderId.toString()) + "/" + requestId, token
                ))
                .andExpect(status().isNoContent());

        verify(requestService).deleteRequest(eq(requestId), anyString());
    }

    @Test
    void shouldReturnForbiddenWhenDeleteRequestThrows() throws Exception {
        // given
        Long requestId = TestUtil.generateRandomId();

        doThrow(new RuntimeException("access denied"))
                .when(requestService).deleteRequest(eq(requestId), anyString());

        // when / then
        mockMvc.perform(JsonTestUtil.deleteAuth(
                        REQUESTS_URL.replace(FOLDER_ID_PLACEHOLDER, folderId.toString()) + "/" + requestId, token
                ))
                .andExpect(status().isForbidden());
    }

    private RequestResponse createRequestResponse() {
        return createRequestResponse(TestUtil.generateRandomId());
    }

    private RequestResponse createRequestResponse(Long requestId) {
        return new RequestResponse(
                requestId,
                TestUtil.generateRandomName(),
                TestUtil.generateRandomDescription(),
                TestUtil.generateRandomRequestMethod(),
                TestUtil.generateRandomUrl(),
                JsonTestUtil.jsonContentTypeHeader(),
                null,
                null,
                null,
                null,
                null,
                LocalDateTime.now(),
                folderId
        );
    }
}
