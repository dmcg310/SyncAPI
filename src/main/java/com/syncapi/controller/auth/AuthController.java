package com.syncapi.controller.auth;

import com.syncapi.dto.auth.AuthResponse;
import com.syncapi.dto.auth.LoginRequest;
import com.syncapi.dto.auth.RegisterRequest;
import com.syncapi.dto.auth.UpdatePasswordRequest;
import com.syncapi.service.auth.AuthService;
import com.syncapi.util.Util;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for authentication-related endpoints.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    /**
     * The authentication service.
     */
    @Autowired
    private AuthService authService;

    /**
     * Registers a new user.
     *
     * @param request the registration request
     * @return the authentication response
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse auth = authService.register(request);

        return ResponseEntity.ok(auth);
    }

    /**
     * Logs in a user.
     *
     * @param request the login request
     * @return the authentication response
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse auth = authService.login(request);

        return ResponseEntity.ok(auth);
    }

    /**
     * Updates the password for the current user.
     *
     * @param request the update password request
     * @return the authentication response
     */
    @PatchMapping("/password")
    public ResponseEntity<AuthResponse> updatePassword(@Valid @RequestBody UpdatePasswordRequest request) {
        AuthResponse auth = authService.updatePassword(request, Util.getCurrentUserEmail());

        return ResponseEntity.ok(auth);
    }
}
