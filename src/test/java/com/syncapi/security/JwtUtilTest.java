package com.syncapi.security;

import com.syncapi.TestUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtUtilTest {
    private static final String SECRET = "0123456789abcdef0123456789abcdef";
    private static final long ONE_HOUR_MS = 60 * 60 * 1000L;

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();

        ReflectionTestUtils.setField(jwtUtil, "secret", SECRET);
        ReflectionTestUtils.setField(jwtUtil, "expiration", ONE_HOUR_MS);
    }

    @Test
    void shouldGenerateTokenAndExtractUsername() {
        // given
        String email = TestUtil.generateRandomEmail();

        // when
        String token = jwtUtil.generateToken(email);

        // then
        assertThat(token).isNotBlank();
        assertThat(jwtUtil.extractUsername(token)).isEqualTo(email);
    }

    @Test
    void shouldExtractExpiration() {
        // given
        long before = System.currentTimeMillis();

        // when
        String token = jwtUtil.generateToken(TestUtil.generateRandomEmail());

        // then
        Date exp = jwtUtil.extractExpiration(token);
        assertThat(exp).isNotNull();
        assertThat(exp.getTime()).isBetween(before + ONE_HOUR_MS - 5_000, before + ONE_HOUR_MS + 5_000);
    }

    @Test
    void shouldValidateTokenWhenEmailMatchesAndNotExpired() {
        // given
        String email = TestUtil.generateRandomEmail();

        // when
        boolean valid = jwtUtil.validateToken(jwtUtil.generateToken(email), email);

        // then
        assertThat(valid).isTrue();
    }

    @Test
    void shouldFailValidationWhenEmailDoesNotMatch() {
        // when
        boolean valid = jwtUtil.validateToken(
                jwtUtil.generateToken(TestUtil.generateRandomEmail()), TestUtil.generateRandomEmail()
        );

        // then
        assertThat(valid).isFalse();
    }

    @Test
    void shouldFailValidationWhenTokenExpired() {
        // given
        JwtUtil shortLived = new JwtUtil();
        ReflectionTestUtils.setField(shortLived, "secret", SECRET);
        ReflectionTestUtils.setField(shortLived, "expiration", 1L); // 1ms

        String email = TestUtil.generateRandomEmail();
        String token = shortLived.generateToken(email);

        // ensure we're past expiry
        try {
            Thread.sleep(5);
        } catch (InterruptedException ignored) {
        }

        // when
        boolean valid = shortLived.validateToken(token, email);

        // then
        assertThat(valid).isFalse();
    }

    @Test
    void shouldThrowForInvalidToken() {
        // given
        String invalidToken = TestUtil.generateRandomToken();

        // when / then
        assertThatThrownBy(() -> jwtUtil.extractUsername(invalidToken))
                .isInstanceOf(Exception.class);
        assertThatThrownBy(() -> jwtUtil.extractExpiration(invalidToken))
                .isInstanceOf(Exception.class);
    }

    @Test
    void shouldThrowWhenTokenSignedWithDifferentSecret() {
        // given
        JwtUtil signer = new JwtUtil();
        ReflectionTestUtils.setField(signer, "secret", TestUtil.generateRandomSecret());
        ReflectionTestUtils.setField(signer, "expiration", ONE_HOUR_MS);

        String email = TestUtil.generateRandomEmail();

        // when / then (verifier has SECRET, signer used different secret)
        assertThatThrownBy(() -> jwtUtil.extractUsername(signer.generateToken(email)))
                .isInstanceOf(Exception.class);
    }
}
