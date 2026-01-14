package com.syncapi.service.auth;

import com.syncapi.dto.auth.AuthResponse;
import com.syncapi.dto.auth.LoginRequest;
import com.syncapi.dto.auth.RegisterRequest;
import com.syncapi.entity.User;
import com.syncapi.repository.UserRepository;
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

    public AuthService(UserRepository userRepository,
                       JwtService jwtService,
                       PasswordEncoder passwordEncoder,
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

        return new AuthResponse(jwtService.generateToken(user.getEmail()), user.getEmail(), user.getName());
    }

    public AuthResponse login(LoginRequest request) {
        User user = util.getUserByEmail(request.getEmail());
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new RuntimeException("Invalid email or password");
        }

        return new AuthResponse(jwtService.generateToken(user.getEmail()), user.getEmail(), user.getName());
    }
}
