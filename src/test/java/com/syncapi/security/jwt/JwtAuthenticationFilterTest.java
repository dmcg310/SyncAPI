package com.syncapi.security.jwt;

import com.syncapi.TestUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {
    private static final String AUTH_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    @Mock
    private JwtService jwtService;

    @Mock
    private FilterChain filterChain;

    private JwtAuthenticationFilter filter;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        filter = new JwtAuthenticationFilter(jwtService);
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();

        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldAuthenticateWithValidToken() throws ServletException, IOException {
        // given
        String token = TestUtil.generateRandomToken();
        request.addHeader(AUTH_HEADER, BEARER_PREFIX + token);

        String email = TestUtil.generateRandomEmail();
        when(jwtService.extractEmail(token)).thenReturn(Optional.of(email));
        when(jwtService.isValid(token, email)).thenReturn(true);

        // when
        filter.doFilterInternal(request, response, filterChain);

        // then
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication().getName()).isEqualTo(email);

        verify(filterChain).doFilter(request, response);
    }

    @Test
    void shouldNotAuthenticateWithInvalidToken() throws ServletException, IOException {
        // given
        String token = TestUtil.generateRandomToken();
        request.addHeader(AUTH_HEADER, BEARER_PREFIX + token);

        when(jwtService.extractEmail(token)).thenReturn(Optional.empty());

        // when
        filter.doFilterInternal(request, response, filterChain);

        // then
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();

        verify(jwtService).extractEmail(token);
        verify(jwtService, never()).isValid(anyString(), anyString());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void shouldNotAuthenticateWhenTokenValidationFails() throws ServletException, IOException {
        // given
        String token = TestUtil.generateRandomToken();
        request.addHeader(AUTH_HEADER, BEARER_PREFIX + token);

        String email = TestUtil.generateRandomEmail();
        when(jwtService.extractEmail(token)).thenReturn(Optional.of(email));
        when(jwtService.isValid(token, email)).thenReturn(false);

        // when
        filter.doFilterInternal(request, response, filterChain);

        // then
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();

        verify(filterChain).doFilter(request, response);
    }

    @Test
    void shouldContinueFilterChainWithoutAuthHeader() throws ServletException, IOException {
        // when
        filter.doFilterInternal(request, response, filterChain);

        // then
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();

        verifyNoInteractions(jwtService);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void shouldIgnoreNonBearerAuthHeader() throws ServletException, IOException {
        // given
        request.addHeader(AUTH_HEADER, "Basic " + TestUtil.generateRandomToken());

        // when
        filter.doFilterInternal(request, response, filterChain);

        // then
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();

        verifyNoInteractions(jwtService);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void shouldNotReauthenticateIfAlreadyAuthenticated() throws ServletException, IOException {
        // given
        String token = TestUtil.generateRandomToken();
        request.addHeader(AUTH_HEADER, BEARER_PREFIX + token);

        String unchangedEmail = TestUtil.generateRandomEmail();

        // pre-authenticate
        UsernamePasswordAuthenticationToken existingAuth =
                new UsernamePasswordAuthenticationToken(unchangedEmail, null, List.of());
        SecurityContextHolder.getContext().setAuthentication(existingAuth);

        when(jwtService.extractEmail(token)).thenReturn(Optional.of(TestUtil.generateRandomEmail()));

        // when
        filter.doFilterInternal(request, response, filterChain);

        // then
        assertThat(SecurityContextHolder.getContext().getAuthentication().getName()).isEqualTo(unchangedEmail);

        verify(jwtService, never()).isValid(anyString(), anyString());
        verify(filterChain).doFilter(request, response);
    }
}
