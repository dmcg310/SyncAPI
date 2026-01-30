package com.syncapi.controller.documentation;

import com.syncapi.JsonTestUtil;
import com.syncapi.TestUtil;
import com.syncapi.dto.documentation.OpenApiInfo;
import com.syncapi.dto.documentation.OpenApiSpec;
import com.syncapi.exception.AccessDeniedException;
import com.syncapi.exception.ResourceNotFoundException;
import com.syncapi.security.jwt.JwtService;
import com.syncapi.service.documentation.DocumentationService;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DocumentationController.class)
@AutoConfigureMockMvc(addFilters = false)
class DocumentationControllerTest {
    private static final String DOCUMENTATION_URL = "/api/workspaces/{workspaceId}/documentation";

    private static final String OPENAPI_VERSION = "3.0.0";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DocumentationService documentationService;

    @MockitoBean
    private JwtService jwtService;

    private Long workspaceId;

    @BeforeEach
    void setUp() {
        workspaceId = TestUtil.generateRandomId();

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(TestUtil.generateRandomEmail(), null, List.of())
        );
    }

    @Test
    void shouldReturnOpenApiSpec() throws Exception {
        // given
        String workspaceName = TestUtil.generateRandomName();
        String workspaceDescription = TestUtil.generateRandomDescription();

        OpenApiInfo info = new OpenApiInfo(workspaceName, workspaceDescription);
        OpenApiSpec spec = new OpenApiSpec(info, Map.of());

        when(documentationService.generateSpec(anyLong(), anyString())).thenReturn(spec);

        // when
        ResultActions res = mockMvc.perform(JsonTestUtil.getJson(
                DOCUMENTATION_URL.replace("{workspaceId}", workspaceId.toString())
        ));

        // then
        res.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpectAll(
                        jsonPath("$.openapi").value(OPENAPI_VERSION),
                        jsonPath("$.info.title").value(workspaceName),
                        jsonPath("$.info.description").value(workspaceDescription),
                        jsonPath("$.paths").isMap()
                );

        verify(documentationService).generateSpec(anyLong(), anyString());
    }

    @Test
    void shouldReturnNotFoundWhenWorkspaceDoesNotExist() throws Exception {
        // given
        when(documentationService.generateSpec(anyLong(), anyString()))
                .thenThrow(new ResourceNotFoundException("Workspace not found: " + workspaceId));

        // when / then
        mockMvc.perform(JsonTestUtil.getJson(
                        DOCUMENTATION_URL.replace("{workspaceId}", workspaceId.toString())
                ))
                .andExpect(status().isNotFound());

        verify(documentationService).generateSpec(anyLong(), anyString());
    }

    @Test
    void shouldReturnForbiddenWhenAccessDenied() throws Exception {
        // given
        when(documentationService.generateSpec(anyLong(), anyString()))
                .thenThrow(new AccessDeniedException("Access denied to workspace: " + workspaceId));

        // when / then
        mockMvc.perform(JsonTestUtil.getJson(
                        DOCUMENTATION_URL.replace("{workspaceId}", workspaceId.toString())
                ))
                .andExpect(status().isForbidden());

        verify(documentationService).generateSpec(anyLong(), anyString());
    }
}
