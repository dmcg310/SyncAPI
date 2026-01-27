package com.syncapi.service.auth;

import com.syncapi.dto.auth.AuthResponse;
import com.syncapi.dto.auth.LoginRequest;
import com.syncapi.dto.auth.RegisterRequest;
import com.syncapi.dto.auth.UpdatePasswordRequest;
import com.syncapi.dto.auth.UserResponse;
import com.syncapi.entity.user.User;
import com.syncapi.exception.ConflictException;
import com.syncapi.exception.UnauthorizedException;
import com.syncapi.repository.user.UserRepository;
import com.syncapi.security.jwt.JwtService;
import com.syncapi.util.Util;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Service for authentication operations.
 */
@Service
public class AuthService {
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final Util util;

    /**
     * Parameterized constructor.
     *
     * @param userRepository  the user repository
     * @param jwtService      the JWT service
     * @param passwordEncoder the password encoder
     * @param util            the utility service
     */
    public AuthService(UserRepository userRepository, JwtService jwtService, PasswordEncoder passwordEncoder,
                       Util util) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
        this.util = util;
    }

    /**
     * Registers a new user.
     *
     * @param request the registration request
     * @return the authentication response
     */
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("Email already exists");
        }

        User user = new User(request.getEmail(), passwordEncoder.encode(request.getPassword()), request.getName());
        userRepository.save(user);

        return toResponse(user);
    }

    /**
     * Authenticates a user.
     *
     * @param request the login request
     * @return the authentication response
     */
    public AuthResponse login(LoginRequest request) {
        User user = util.getUserByEmail(request.getEmail());
        if (!isPasswordCorrect(request.getPassword(), user.getPasswordHash())) {
            throw new UnauthorizedException("Invalid email or password");
        }

        return toResponse(user);
    }

    /**
     * Updates a user's password.
     *
     * @param request the update password request
     * @param email   the user's email
     * @return the authentication response
     */
    public AuthResponse updatePassword(UpdatePasswordRequest request, String email) {
        User user = util.getUserByEmail(email);
        if (!isPasswordCorrect(request.getOriginalPassword(), user.getPasswordHash())) {
            throw new UnauthorizedException("Original password is incorrect");
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        return toResponse(user);
    }

    /**
     * Gets the current user's profile.
     *
     * @param email the user's email
     * @return the user response
     */
    public UserResponse getCurrentUser(String email) {
        User user = util.getUserByEmail(email);

        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getCreatedAt()
        );
    }

    /**
     * Checks if a password matches the stored hash.
     *
     * @param password            the plain text password
     * @param currentPasswordHash the stored password hash
     * @return true if the password is correct
     */
    private boolean isPasswordCorrect(String password, String currentPasswordHash) {
        return passwordEncoder.matches(password, currentPasswordHash);
    }

    /**
     * Converts a user entity to an authentication response.
     *
     * @param user the user entity
     * @return the authentication response
     */
    private AuthResponse toResponse(User user) {
        return new AuthResponse(
                jwtService.generateToken(user.getEmail()),
                user.getEmail(),
                user.getName()
        );
    }
}
