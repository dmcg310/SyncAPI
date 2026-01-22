package com.syncapi.service.auth;

import com.syncapi.dto.auth.AuthResponse;
import com.syncapi.dto.auth.LoginRequest;
import com.syncapi.dto.auth.RegisterRequest;
import com.syncapi.dto.auth.UpdatePasswordRequest;
import com.syncapi.entity.User;
import com.syncapi.repository.user.UserRepository;
import com.syncapi.security.jwt.JwtService;
import com.syncapi.util.Util;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final Util util;

    public AuthService(UserRepository userRepository, JwtService jwtService, PasswordEncoder passwordEncoder,
                       Util util) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
        this.util = util;
    }

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        User user = new User(request.getEmail(), passwordEncoder.encode(request.getPassword()), request.getName());
        userRepository.save(user);

        return toResponse(user);
    }

    public AuthResponse login(LoginRequest request) {
        User user = util.getUserByEmail(request.getEmail());
        if (!isPasswordCorrect(request.getPassword(), user.getPasswordHash())) {
            throw new RuntimeException("Invalid email or password");
        }

        return toResponse(user);
    }

    public AuthResponse updatePassword(UpdatePasswordRequest request, String email) {
        User user = util.getUserByEmail(email);
        if (!isPasswordCorrect(request.getOriginalPassword(), user.getPasswordHash())) {
            throw new RuntimeException("Current password is incorrect");
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        return toResponse(user);
    }

    private boolean isPasswordCorrect(String password, String currentPasswordHash) {
        return passwordEncoder.matches(password, currentPasswordHash);
    }

    private AuthResponse toResponse(User user) {
        return new AuthResponse(
                jwtService.generateToken(user.getEmail()),
                user.getEmail(),
                user.getName()
        );
    }

}
