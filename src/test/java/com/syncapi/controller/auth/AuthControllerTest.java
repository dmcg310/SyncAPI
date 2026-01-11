package com.syncapi.controller.auth;

import com.syncapi.AbstractIntegrationTest;
import com.syncapi.dto.auth.LoginRequest;
import com.syncapi.dto.auth.RegisterRequest;
import com.syncapi.entity.User;
import com.syncapi.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.stream.Stream;

import static com.syncapi.TestUtil.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
class AuthControllerTest extends AbstractIntegrationTest {
    private static final String AUTH_URL = "/api/auth";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    void shouldRegisterNewUser() throws Exception {
        // given
        String name = generateRandomName();
        String email = generateRandomEmail();
        RegisterRequest request = new RegisterRequest(name, email, generateRandomPassword());

        // when
        ResultActions res = mockMvc.perform(postJson(AUTH_URL + "/register", request));

        // then
        res.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.token").isString())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.name").value(name));

        User user = userRepository.findByEmail(email).orElseThrow();
        assertThat(user.getName()).isEqualTo(name);
        assertThat(user.getPasswordHash()).isNotBlank();
    }

    @Test
    void shouldFailWhenEmailAlreadyExists() throws Exception {
        // given
        String email = generateRandomEmail();
        RegisterRequest first = new RegisterRequest(generateRandomName(), email, generateRandomPassword());
        RegisterRequest second = new RegisterRequest(generateRandomName(), email, generateRandomPassword());

        // when / then
        mockMvc.perform(postJson(AUTH_URL + "/register", first))
                .andExpect(status().isOk());
        mockMvc.perform(postJson(AUTH_URL + "/register", second))
                .andExpect(status().isBadRequest());
    }

    @ParameterizedTest(name = "[{index}] invalid register request -> 400")
    @MethodSource("invalidRegisterRequests")
    void shouldFailValidationForInvalidRegisterRequest(RegisterRequest request) throws Exception {
        // when / then
        mockMvc.perform(postJson(AUTH_URL + "/register", request))
                .andExpect(status().isBadRequest());
    }

    private static Stream<RegisterRequest> invalidRegisterRequests() {
        return Stream.of(
                new RegisterRequest("", generateRandomEmail(), generateRandomPassword()), // blank name
                new RegisterRequest(null, generateRandomEmail(), generateRandomPassword()), // null name
                new RegisterRequest("John", "not-an-email", generateRandomPassword()), // invalid email
                new RegisterRequest("John", "", generateRandomPassword()), // blank email
                new RegisterRequest("John", generateRandomEmail(), "123") // short password
        );
    }

    @Test
    void shouldLoginSuccessfully() throws Exception {
        // given
        String email = generateRandomEmail();
        String password = generateRandomPassword();
        RegisterRequest register = new RegisterRequest("Login User", email, password);

        mockMvc.perform(postJson(AUTH_URL + "/register", register))
                .andExpect(status().isOk());

        LoginRequest login = new LoginRequest(email, password);

        // when
        ResultActions res = mockMvc.perform(postJson(AUTH_URL + "/login", login));

        // then
        res.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.token").isString())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.email").value(email));
    }

    @ParameterizedTest(name = "[{index}] invalid login -> 400")
    @MethodSource("invalidLoginRequests")
    void shouldFailLoginForInvalidCredentials(LoginRequest request) throws Exception {
        // when / then
        mockMvc.perform(postJson(AUTH_URL + "/login", request))
                .andExpect(status().isBadRequest());
    }

    private static Stream<LoginRequest> invalidLoginRequests() {
        return Stream.of(
                new LoginRequest("not-an-email", "password"), // invalid email
                new LoginRequest("", "password"), // blank email
                new LoginRequest(generateRandomEmail(), ""), // blank password
                new LoginRequest(generateRandomEmail(), "wrong-password") // wrong password
        );
    }
}
