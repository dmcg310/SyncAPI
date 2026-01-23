package com.syncapi.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Optional;

/**
 * Service for JWT token generation and validation.
 */
@Service
public class JwtService {
    private static final Logger log = LoggerFactory.getLogger(JwtService.class);

    private final String secret;
    private final long expirationMs;

    /**
     * Parameterized constructor.
     *
     * @param secret       the JWT secret key
     * @param expirationMs the token expiration time in milliseconds
     */
    public JwtService(@Value("${jwt.secret}") String secret, @Value("${jwt.expiration}") long expirationMs) {
        this.secret = secret;
        this.expirationMs = expirationMs;
    }

    /**
     * Gets the signing key for JWT operations.
     *
     * @return the secret key
     */
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Generates a JWT token for a user.
     *
     * @param email the user's email
     * @return the generated token
     */
    public String generateToken(String email) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .subject(email)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Extracts the email from a JWT token.
     *
     * @param token the JWT token
     * @return an optional containing the email if valid
     */
    public Optional<String> extractEmail(String token) {
        return parseClaims(token).map(Claims::getSubject);
    }

    /**
     * Validates a JWT token for a given email.
     *
     * @param token the JWT token
     * @param email the user's email
     * @return true if the token is valid
     */
    public boolean isValid(String token, String email) {
        return extractEmail(token)
                .map(extracted -> extracted.equals(email) && !isExpired(token))
                .orElse(false);
    }

    /**
     * Checks if a JWT token is expired.
     *
     * @param token the JWT token
     * @return true if the token is expired
     */
    private boolean isExpired(String token) {
        return parseClaims(token)
                .map(claims -> claims.getExpiration().before(new Date()))
                .orElse(true);
    }

    /**
     * Parses claims from a JWT token.
     *
     * @param token the JWT token
     * @return an optional containing the claims if valid
     */
    private Optional<Claims> parseClaims(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            return Optional.of(claims);
        } catch (JwtException e) {
            log.debug("Invalid JWT token: {}", e.getMessage());

            return Optional.empty();
        }
    }
}