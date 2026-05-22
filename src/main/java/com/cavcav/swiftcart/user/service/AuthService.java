package com.cavcav.swiftcart.user.service;


import com.cavcav.swiftcart.common.exception.BusinessException;
import com.cavcav.swiftcart.notfication.service.EmailService;
import com.cavcav.swiftcart.user.dto.request.*;
import com.cavcav.swiftcart.user.dto.response.*;

import com.cavcav.swiftcart.user.model.User;
import com.cavcav.swiftcart.user.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import javax.naming.AuthenticationException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Objects;

@Service
@Slf4j
public class AuthService {
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final RedisTemplate<String, Object> redisTemplate;


    public AuthService(UserRepository userRepository, EmailService emailService, RedisTemplate<String, Object> redisTemplate) {
        this.userRepository = userRepository;
        this.emailService = emailService;
        this.redisTemplate = redisTemplate;
    }

    public AuthResponse signup(RegisterRequest registerRequest) {
        log.info("Signup request received for email: {}", registerRequest.email());
        User newUser = new User(registerRequest.email(), registerRequest.password());
        User savedUser = userRepository.save(newUser);
        String token=generateRandomToken();
        redisTemplate.opsForValue().set("verify:" + token, savedUser.getId());
        emailService.sendVerificationEmail(savedUser.getEmail(),token);
        log.info(
                "User successfully registered with id: {} and email: {}",
                savedUser.getId(),
                savedUser.getEmail()
        );
        return AuthResponse.of(null, UserResponse.from(savedUser));
    }

    public AuthResponse login(LoginRequest loginRequest) throws AuthenticationException {
        log.info("Login attempt for email: {}", loginRequest.email());
        User findUser = userRepository.findByEmail(loginRequest.email()).orElse(null);

        if (findUser == null || !findUser.getPassword().equals(loginRequest.password())){

            log.warn("Failed login attempt for email: {}",loginRequest.email());

            throw new AuthenticationException("Invalid email or password");
        }
        log.info("User logged in successfully. User id: {}, email: {}", findUser.getId(), findUser.getEmail());

        return AuthResponse.of("Token", UserResponse.from(findUser));
    }

    public void verifyEmail(String token){
        log.info("Email verification request received: token={}", token);

        String userId = Objects.requireNonNull(redisTemplate.opsForValue().get("verify:" + token)).toString();

        if (userId == null) {
            log.warn("Invalid or expired verification token: token={}", token);
            throw new BusinessException(
                    "Invalid or expired verification token",
                    "INVALID_TOKEN",
                    HttpStatus.BAD_REQUEST
            );
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("User not found for verification: userId={}", userId);
                    return new BusinessException(
                            "User not found",
                            "USER_NOT_FOUND",
                            HttpStatus.NOT_FOUND
                    );
                });

        user.setIsActive(true);
        user.setIsEmailVerified(true);
        userRepository.save(user);

        redisTemplate.delete("verify:" + token);

        log.info("Email verified successfully: userId={}, email={}", user.getId(), user.getEmail());
    }


    private String generateRandomToken() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
