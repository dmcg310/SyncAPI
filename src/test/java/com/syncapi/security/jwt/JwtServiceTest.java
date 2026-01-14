package com.syncapi.security.jwt;

import com.syncapi.TestUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {
    private static final String SECRET = "0123456789abcdef0123456789abcdef";
    private static final long ONE_HOUR_MS = 60 * 60 * 1000L;

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(SECRET, ONE_HOUR_MS);
    }

    @Test
    void shouldGenerateToken() {
        // given
        String email = TestUtil.generateRandomEmail();

        // when
        String token = jwtService.generateToken(email);

        // then
        assertThat(token).isNotBlank();
        assertThat(token.split("\\.")).hasSize(3); // JWT has 3 parts
    }

    @Test
    void shouldExtractEmailFromToken() {
        // given
        String email = TestUtil.generateRandomEmail();
        String token = jwtService.generateToken(email);

        // when
        Optional<String> extracted = jwtService.extractEmail(token);

        // then
        assertThat(extracted).isPresent();
        assertThat(extracted.get()).isEqualTo(email);
    }

    @Test
    void shouldReturnEmptyForInvalidToken() {
        // when
        Optional<String> extracted = jwtService.extractEmail(TestUtil.generateRandomToken());

        // then
        assertThat(extracted).isEmpty();
    }

    @Test
    void shouldReturnEmptyForMalformedToken() {
        // when
        Optional<String> extracted = jwtService.extractEmail(TestUtil.generateRandomToken());

        // then
        assertThat(extracted).isEmpty();
    }

    @Test
    void shouldValidateTokenWithMatchingEmail() {
        // given
        String email = TestUtil.generateRandomEmail();
        String token = jwtService.generateToken(email);

        // when
        boolean valid = jwtService.isValid(token, email);

        // then
        assertThat(valid).isTrue();
    }

    @Test
    void shouldRejectTokenWithDifferentEmail() {
        // given
        String token = jwtService.generateToken(TestUtil.generateRandomEmail());

        // when
        boolean valid = jwtService.isValid(token, TestUtil.generateRandomEmail());

        // then
        assertThat(valid).isFalse();
    }

    @Test
    void shouldRejectExpiredToken() throws InterruptedException {
        // given
        JwtService shortLivedService = new JwtService(SECRET, 1L); // 1ms expiry
        String email = TestUtil.generateRandomEmail();
        String token = shortLivedService.generateToken(email);

        Thread.sleep(10); // wait for expiry

        // when
        boolean valid = shortLivedService.isValid(token, email);

        // then
        assertThat(valid).isFalse();
    }

    @Test
    void shouldRejectTokenSignedWithDifferentSecret() {
        // given
        JwtService otherService = new JwtService(TestUtil.generateRandomSecret(), ONE_HOUR_MS);
        String email = TestUtil.generateRandomEmail();
        String token = otherService.generateToken(email);

        // when
        boolean valid = jwtService.isValid(token, email);

        // then
        assertThat(valid).isFalse();
    }

    @Test
    void shouldRejectInvalidTokenInValidation() {
        // when
        boolean valid = jwtService.isValid(TestUtil.generateRandomToken(), TestUtil.generateRandomEmail());

        // then
        assertThat(valid).isFalse();
    }
}
