package com.cavcav.swiftcart.auth.service;


import com.cavcav.swiftcart.auth.dto.UserRegisteredEvent;
import com.cavcav.swiftcart.auth.dto.request.LoginRequest;
import com.cavcav.swiftcart.auth.dto.request.RegisterRequest;
import com.cavcav.swiftcart.auth.dto.response.LoginResponse;
import com.cavcav.swiftcart.auth.dto.response.SignupResponse;
import com.cavcav.swiftcart.auth.security.UserPrincipal;
import com.cavcav.swiftcart.common.exception.BusinessException;

import com.cavcav.swiftcart.common.service.RateLimitService;
import com.cavcav.swiftcart.user.dto.response.UserResponse;
import com.cavcav.swiftcart.user.model.User;
import com.cavcav.swiftcart.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final RedisTemplate<String, String> redisTemplate;
    private final RateLimitService rateLimitService;

    public SignupResponse signup(RegisterRequest request,String ip) {
        rateLimitService.checkSignupLimit(ip);
        log.info("Signup request: email={}", request.email());

        if (userRepository.existsByEmail(request.email())) {
            log.warn("Signup failed - email exists: email={}", request.email());
            throw new BusinessException(
                    "Email already exists",
                    "EMAIL_ALREADY_EXISTS",
                    HttpStatus.CONFLICT
            );
        }

        User user = new User(request.email(), passwordEncoder.encode(request.password()));
        User saved = userRepository.save(user);


        eventPublisher.publishEvent(new UserRegisteredEvent(saved));

        log.info("User registered: id={}, email={}", saved.getId(), saved.getEmail());
        return SignupResponse.from(saved);
    }

    public LoginResponse login(LoginRequest request) {
        rateLimitService.checkLoginLimit(request.email());
        log.info("Login attempt: email={}", request.email());

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.email(),
                        request.password()
                ));

        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        User user = principal.user();

        String accessToken = jwtService.generateAccessToken(
                user.getEmail(), user.getId(), String.valueOf(user.getRole()));
        String refreshToken = jwtService.generateRefreshToken(
                user.getEmail(), user.getId());

        log.info("Login successful: id={}, email={}", user.getId(), user.getEmail());
        return LoginResponse.of(accessToken, refreshToken, UserResponse.from(user));
    }

    public void logout(String accessHeader, String refreshHeader) {


        if (accessHeader != null && accessHeader.startsWith("Bearer ")) {
            String accessToken = accessHeader.substring(7);
            long accessExpiration = jwtService.getAccessTokenExpiration(accessToken);
            if (accessExpiration > 0) {
                redisTemplate.opsForValue().set(
                        "blacklist:" + accessToken,
                        "true",
                        accessExpiration,
                        TimeUnit.MILLISECONDS
                );
            }
        }

        if (refreshHeader != null && refreshHeader.startsWith("Bearer ")) {
            String refreshToken = refreshHeader.substring(7);
            long refreshExpiration = jwtService.getRefreshTokenExpiration(refreshToken);
            if (refreshExpiration > 0) {
                redisTemplate.opsForValue().set(
                        "blacklist:" + refreshToken,
                        "true",
                        refreshExpiration,
                        TimeUnit.MILLISECONDS
                );
            }
        }

        log.info("User logged out successfully");
    }


    public LoginResponse refresh(String refreshHeader) {
        rateLimitService.checkRefreshLimit(refreshHeader);
        log.info("Token refresh request received");
        if (refreshHeader == null || !refreshHeader.startsWith("Bearer ")) {
            throw new BusinessException(
                    "Invalid authorization header",
                    "INVALID_AUTH_HEADER",
                    HttpStatus.BAD_REQUEST
            );
        }
        String refreshToken = refreshHeader.substring(7);

        if (redisTemplate.hasKey("blacklist:" + refreshToken)) {
            log.warn("Blacklisted refresh token used");
            throw new BusinessException(
                    "Refresh token has been invalidated",
                    "INVALID_REFRESH_TOKEN",
                    HttpStatus.UNAUTHORIZED
            );
        }
        if (!jwtService.isRefreshTokenValid(refreshToken)) {
            log.warn("Invalid or expired refresh token");
            throw new BusinessException(
                    "Invalid or expired refresh token",
                    "INVALID_REFRESH_TOKEN",
                    HttpStatus.UNAUTHORIZED
            );
        }
        String email = jwtService.extractEmailFromRefreshToken(refreshToken);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("User not found for refresh: email={}", email);
                    return new BusinessException(
                            "User not found",
                            "USER_NOT_FOUND",
                            HttpStatus.NOT_FOUND
                    );
                });
        if (!user.getIsActive() || !user.getIsEmailVerified()) {
            log.warn("Inactive user tried to refresh token: email={}", email);
            throw new BusinessException(
                    "Account is not active",
                    "ACCOUNT_NOT_ACTIVE",
                    HttpStatus.FORBIDDEN
            );
        }
        String newAccessToken = jwtService.generateAccessToken(
                user.getEmail(),
                user.getId(),
                String.valueOf(user.getRole())
        );

        String newRefreshToken = jwtService.generateRefreshToken(
                user.getEmail(), user.getId());

        long oldExpiration = jwtService.getRefreshTokenExpiration(refreshToken);
        if (oldExpiration > 0) {
            redisTemplate.opsForValue().set(
                    "blacklist:" + refreshToken,
                    "true",
                    oldExpiration,
                    TimeUnit.MILLISECONDS
            );
        }
        log.info("Token refreshed successfully: email={}", email);
        return LoginResponse.of(newAccessToken, newRefreshToken, UserResponse.from(user));
    }
}


