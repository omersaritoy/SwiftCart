package com.cavcav.swiftcart.user.service;


import com.cavcav.swiftcart.common.exception.BusinessException;
import com.cavcav.swiftcart.notfication.service.EmailService;
import com.cavcav.swiftcart.notfication.service.EmailVerificationService;
import com.cavcav.swiftcart.user.dto.request.*;
import com.cavcav.swiftcart.user.dto.response.*;

import com.cavcav.swiftcart.user.model.User;
import com.cavcav.swiftcart.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final EmailVerificationService emailVerificationService;

    public AuthResponse signup(RegisterRequest request) {
        log.info("Signup request: email={}", request.email());

        if (userRepository.existsByEmail(request.email())) {
            log.warn("Signup failed - email exists: email={}", request.email());
            throw new BusinessException(
                    "Email already exists",
                    "EMAIL_ALREADY_EXISTS",
                    HttpStatus.CONFLICT
            );
        }

        User user = new User(request.email(), request.password());
        User saved = userRepository.save(user);

        emailVerificationService.sendVerificationEmail(saved);

        log.info("User registered: id={}, email={}", saved.getId(), saved.getEmail());
        return AuthResponse.of(null, UserResponse.from(saved));
    }

    public AuthResponse login(LoginRequest request) {
        log.info("Login attempt: email={}", request.email());

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> {
                    log.warn("Login failed - not found: email={}", request.email());
                    return new BusinessException(
                            "Invalid credentials",
                            "INVALID_CREDENTIALS",
                            HttpStatus.UNAUTHORIZED
                    );
                });

        if (request.password().equals(user.getPassword())) {
            log.warn("Login failed - wrong password: email={}", request.email());
            throw new BusinessException(
                    "Invalid credentials",
                    "INVALID_CREDENTIALS",
                    HttpStatus.UNAUTHORIZED
            );
        }

        if (!user.getIsEmailVerified()) {
            log.warn("Login failed - email not verified: email={}", request.email());
            throw new BusinessException(
                    "Please verify your email first",
                    "EMAIL_NOT_VERIFIED",
                    HttpStatus.FORBIDDEN
            );
        }

        if (!user.getIsActive()) {
            log.warn("Login failed - deactivated: email={}", request.email());
            throw new BusinessException(
                    "Account is deactivated",
                    "ACCOUNT_DEACTIVATED",
                    HttpStatus.FORBIDDEN
            );
        }

        log.info("Login successful: id={}, email={}", user.getId(), user.getEmail());
        return AuthResponse.of("token-will-be-added", UserResponse.from(user));
    }
}
