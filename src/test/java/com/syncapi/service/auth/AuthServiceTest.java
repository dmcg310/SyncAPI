package com.syncapi.service.auth;

import com.syncapi.TestUtil;
import com.syncapi.dto.auth.AuthResponse;
import com.syncapi.dto.auth.LoginRequest;
import com.syncapi.dto.auth.RegisterRequest;
import com.syncapi.dto.auth.UpdatePasswordRequest;
import com.syncapi.entity.User;
import com.syncapi.repository.user.UserRepository;
import com.syncapi.security.jwt.JwtService;
import com.syncapi.util.Util;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtService jwtService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private Util util;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(userRepository, jwtService, passwordEncoder, util);
    }

    @Test
    void shouldRegisterNewUser() {
        // given
        String name = TestUtil.generateRandomName();
        String email = TestUtil.generateRandomEmail();
        String password = TestUtil.generateRandomPassword();
        RegisterRequest request = new RegisterRequest(name, email, password);

        String encodedPassword = TestUtil.generateRandomPasswordHash();
        String token = TestUtil.generateRandomToken();

        when(userRepository.existsByEmail(email)).thenReturn(false);
        when(passwordEncoder.encode(password)).thenReturn(encodedPassword);
        when(jwtService.generateToken(email)).thenReturn(token);

        // when
        AuthResponse response = authService.register(request);

        // then
        assertThat(response.getToken()).isEqualTo(token);
        assertThat(response.getEmail()).isEqualTo(email);
        assertThat(response.getName()).isEqualTo(name);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();
        assertThat(savedUser.getEmail()).isEqualTo(email);
        assertThat(savedUser.getName()).isEqualTo(name);
        assertThat(savedUser.getPasswordHash()).isEqualTo(encodedPassword);

        verify(passwordEncoder).encode(password);
        verify(jwtService).generateToken(email);
    }

    @Test
    void shouldThrowWhenEmailAlreadyExists() {
        // given
        String email = TestUtil.generateRandomEmail();
        RegisterRequest request = new RegisterRequest(TestUtil.generateRandomName(), email,
                TestUtil.generateRandomPassword());

        when(userRepository.existsByEmail(email)).thenReturn(true);

        // when / then
        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Email already exists");

        verify(userRepository).existsByEmail(email);
        verify(userRepository, never()).save(any());
        verifyNoInteractions(jwtService);
    }

    @Test
    void shouldLoginSuccessfully() {
        // given
        String email = TestUtil.generateRandomEmail();
        String encodedPassword = TestUtil.generateRandomPasswordHash();
        String name = TestUtil.generateRandomName();
        User user = new User(email, encodedPassword, name);

        String password = TestUtil.generateRandomPassword();
        String token = TestUtil.generateRandomToken();

        when(util.getUserByEmail(email)).thenReturn(user);
        when(passwordEncoder.matches(password, encodedPassword)).thenReturn(true);
        when(jwtService.generateToken(email)).thenReturn(token);

        LoginRequest request = new LoginRequest(email, password);

        // when
        AuthResponse response = authService.login(request);

        // then
        assertThat(response.getToken()).isEqualTo(token);
        assertThat(response.getEmail()).isEqualTo(email);
        assertThat(response.getName()).isEqualTo(name);

        verify(util).getUserByEmail(email);
        verify(passwordEncoder).matches(password, encodedPassword);
        verify(jwtService).generateToken(email);
    }

    @Test
    void shouldThrowWhenUserNotFound() {
        // given
        String email = TestUtil.generateRandomEmail();
        LoginRequest request = new LoginRequest(email, TestUtil.generateRandomPassword());

        when(util.getUserByEmail(email)).thenThrow(new RuntimeException("User not found: " + email));

        // when / then
        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("User not found");

        verify(util).getUserByEmail(email);
        verifyNoInteractions(passwordEncoder);
        verifyNoInteractions(jwtService);
    }

    @Test
    void shouldThrowWhenPasswordDoesNotMatch() {
        // given
        String email = TestUtil.generateRandomEmail();
        User user = TestUtil.createUser(TestUtil.generateRandomId(), email, TestUtil.generateRandomName());

        String wrongPassword = TestUtil.generateRandomPassword();
        LoginRequest request = new LoginRequest(email, wrongPassword);

        when(util.getUserByEmail(email)).thenReturn(user);
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        // when / then
        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Invalid email or password");

        verify(util).getUserByEmail(email);
        verify(passwordEncoder).matches(wrongPassword, user.getPasswordHash());
        verifyNoInteractions(jwtService);
    }

    @Test
    void shouldUpdatePasswordSuccessfully() {
        // given
        String email = TestUtil.generateRandomEmail();
        String currentPasswordHash = TestUtil.generateRandomPasswordHash();
        String name = TestUtil.generateRandomName();
        User user = new User(email, currentPasswordHash, name);

        String originalPassword = TestUtil.generateRandomPassword();
        String newPassword = TestUtil.generateRandomPassword();
        String newPasswordHash = TestUtil.generateRandomPasswordHash();
        String token = TestUtil.generateRandomToken();

        UpdatePasswordRequest request = new UpdatePasswordRequest(originalPassword, newPassword);

        when(util.getUserByEmail(email)).thenReturn(user);
        when(passwordEncoder.matches(originalPassword, currentPasswordHash)).thenReturn(true);
        when(passwordEncoder.encode(newPassword)).thenReturn(newPasswordHash);
        when(jwtService.generateToken(email)).thenReturn(token);

        // when
        AuthResponse response = authService.updatePassword(request, email);

        // then
        assertThat(response.getToken()).isEqualTo(token);
        assertThat(response.getEmail()).isEqualTo(email);
        assertThat(response.getName()).isEqualTo(name);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertThat(userCaptor.getValue().getPasswordHash()).isEqualTo(newPasswordHash);

        verify(util).getUserByEmail(email);
        verify(passwordEncoder).matches(originalPassword, currentPasswordHash);
        verify(passwordEncoder).encode(newPassword);
        verify(jwtService).generateToken(email);
    }

    @Test
    void shouldThrowWhenOriginalPasswordIsIncorrect() {
        // given
        String email = TestUtil.generateRandomEmail();
        User user = TestUtil.createUser(TestUtil.generateRandomId(), email, TestUtil.generateRandomName());

        String wrongOriginalPassword = TestUtil.generateRandomPassword();
        UpdatePasswordRequest request = new UpdatePasswordRequest(wrongOriginalPassword,
                TestUtil.generateRandomPassword());

        when(util.getUserByEmail(email)).thenReturn(user);
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        // when / then
        assertThatThrownBy(() -> authService.updatePassword(request, email))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Current password is incorrect");

        verify(util).getUserByEmail(email);
        verify(passwordEncoder).matches(wrongOriginalPassword, user.getPasswordHash());
        verify(userRepository, never()).save(any());
        verifyNoInteractions(jwtService);
    }

    @Test
    void shouldThrowWhenUserNotFoundDuringPasswordUpdate() {
        // given
        String email = TestUtil.generateRandomEmail();
        UpdatePasswordRequest request = new UpdatePasswordRequest(
                TestUtil.generateRandomPassword(),
                TestUtil.generateRandomPassword()
        );

        when(util.getUserByEmail(email)).thenThrow(new RuntimeException("User not found: " + email));

        // when / then
        assertThatThrownBy(() -> authService.updatePassword(request, email))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("User not found");

        verify(util).getUserByEmail(email);
        verifyNoInteractions(passwordEncoder);
        verifyNoInteractions(jwtService);
        verify(userRepository, never()).save(any());
    }
}
