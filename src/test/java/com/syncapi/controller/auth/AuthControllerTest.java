package com.syncapi.controller.auth;

import com.syncapi.JsonTestUtil;
import com.syncapi.TestUtil;
import com.syncapi.dto.auth.AuthResponse;
import com.syncapi.dto.auth.LoginRequest;
import com.syncapi.dto.auth.RegisterRequest;
import com.syncapi.repository.UserRepository;
import com.syncapi.security.JwtUtil;
import com.syncapi.service.auth.AuthService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {
    private static final String AUTH_URL = "/api/auth";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    // required as JwtAuthenticationFilter dependency
    @MockitoBean
    private JwtUtil jwtUtil;
    @MockitoBean
    private UserRepository userRepository;

    @Test
    void shouldRegisterNewUser() throws Exception {
        // given
        String name = TestUtil.generateRandomName();
        String email = TestUtil.generateRandomEmail();
        RegisterRequest request = new RegisterRequest(name, email, TestUtil.generateRandomPassword());

        String token = TestUtil.generateRandomToken();
        AuthResponse response = new AuthResponse(token, email, name);

        when(authService.register(any(RegisterRequest.class)))
                .thenReturn(response);

        // when
        ResultActions res = mockMvc.perform(JsonTestUtil.postJson(AUTH_URL + "/register", request));

        // then
        res.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpectAll(
                        jsonPath("$.token").value(token),
                        jsonPath("$.email").value(email),
                        jsonPath("$.name").value(name)
                );

        verify(authService).register(any(RegisterRequest.class));
    }

    @Test
    void shouldFailWhenEmailAlreadyExists() throws Exception {
        // given
        RegisterRequest request = new RegisterRequest(
                TestUtil.generateRandomName(), TestUtil.generateRandomEmail(), TestUtil.generateRandomPassword()
        );

        when(authService.register(any(RegisterRequest.class)))
                .thenThrow(new RuntimeException("email already exists"));

        // when / then
        mockMvc.perform(JsonTestUtil.postJson(AUTH_URL + "/register", request))
                .andExpect(status().isBadRequest());

        verify(authService).register(any(RegisterRequest.class));
    }

    @ParameterizedTest(name = "[{index}] invalid register request -> 400")
    @MethodSource("invalidRegisterRequests")
    void shouldFailValidationForInvalidRegisterRequest(RegisterRequest request) throws Exception {
        // when / then
        mockMvc.perform(JsonTestUtil.postJson(AUTH_URL + "/register", request))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(authService);
    }

    private static Stream<RegisterRequest> invalidRegisterRequests() {
        return Stream.of(
                new RegisterRequest("", TestUtil.generateRandomEmail(), TestUtil.generateRandomPassword()),
                new RegisterRequest(null, TestUtil.generateRandomEmail(), TestUtil.generateRandomPassword()),
                new RegisterRequest("John", "not-an-email", TestUtil.generateRandomPassword()),
                new RegisterRequest("John", "", TestUtil.generateRandomPassword()),
                new RegisterRequest("John", TestUtil.generateRandomEmail(), "123")
        );
    }

    @Test
    void shouldLoginSuccessfully() throws Exception {
        // given
        String email = TestUtil.generateRandomEmail();
        String token = TestUtil.generateRandomToken();

        LoginRequest request = new LoginRequest(email, TestUtil.generateRandomPassword());
        AuthResponse response = new AuthResponse(token, email, TestUtil.generateRandomName());

        when(authService.login(any(LoginRequest.class)))
                .thenReturn(response);

        // when
        ResultActions res = mockMvc.perform(JsonTestUtil.postJson(AUTH_URL + "/login", request));

        // then
        res.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpectAll(
                        jsonPath("$.token").value(token),
                        jsonPath("$.email").value(email)
                );

        verify(authService).login(any(LoginRequest.class));
    }

    @ParameterizedTest(name = "[{index}] invalid login -> 400")
    @MethodSource("invalidLoginRequests")
    void shouldFailLoginForInvalidCredentials(LoginRequest request) throws Exception {
        boolean passesValidation = request.getEmail() != null
                && !request.getEmail().isBlank()
                && request.getEmail().contains("@")
                && request.getPassword() != null
                && !request.getPassword().isBlank();
        if (passesValidation) {
            when(authService.login(any(LoginRequest.class)))
                    .thenThrow(new RuntimeException("invalid credentials"));
        }

        mockMvc.perform(JsonTestUtil.postJson(AUTH_URL + "/login", request))
                .andExpect(status().isBadRequest());

        if (passesValidation) {
            verify(authService).login(any(LoginRequest.class));
        } else {
            verifyNoInteractions(authService);
        }
    }

    private static Stream<LoginRequest> invalidLoginRequests() {
        return Stream.of(
                new LoginRequest("not-an-email", "password"),
                new LoginRequest("", "password"),
                new LoginRequest(TestUtil.generateRandomEmail(), ""),
                new LoginRequest(TestUtil.generateRandomEmail(), "wrong-password")
        );
    }
}
