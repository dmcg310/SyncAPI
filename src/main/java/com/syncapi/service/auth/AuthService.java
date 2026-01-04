package com.syncapi.service.auth;

import com.syncapi.dto.auth.AuthResponse;
import com.syncapi.dto.auth.LoginRequest;
import com.syncapi.dto.auth.RegisterRequest;
import com.syncapi.entity.User;
import com.syncapi.repository.UserRepository;
import com.syncapi.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        User user = createUser(request.getEmail(), request.getName(), request.getPassword());

        return new AuthResponse(createToken(user.getEmail()), user.getEmail(), user.getName());
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new RuntimeException("Invalid email or password");
        }

        return new AuthResponse(createToken(user.getEmail()), user.getEmail(), user.getName());
    }

    private User createUser(String email, String name, String password) {
        User user = new User();
        user.setEmail(email);
        user.setName(name);
        user.setPasswordHash(passwordEncoder.encode(password));

        return userRepository.save(user);
    }

    private String createToken(String email) {
        return jwtUtil.generateToken(email);
    }
}
