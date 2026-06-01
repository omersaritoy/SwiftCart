package com.cavcav.swiftcart.auth.service;


import com.cavcav.swiftcart.auth.dto.request.LoginRequest;
import com.cavcav.swiftcart.auth.dto.request.RegisterRequest;
import com.cavcav.swiftcart.auth.dto.response.AuthResponse;
import com.cavcav.swiftcart.auth.security.UserPrincipal;
import com.cavcav.swiftcart.common.config.AuthenticationEntryPointConfig;
import com.cavcav.swiftcart.common.exception.BusinessException;
import com.cavcav.swiftcart.notfication.service.EmailVerificationService;

import com.cavcav.swiftcart.user.dto.response.UserResponse;
import com.cavcav.swiftcart.user.model.User;
import com.cavcav.swiftcart.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final EmailVerificationService emailVerificationService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

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

        User user = new User(request.email(), passwordEncoder.encode(request.password()));
        User saved = userRepository.save(user);

        emailVerificationService.sendVerificationEmail(saved);

        log.info("User registered: id={}, email={}", saved.getId(), saved.getEmail());
        return AuthResponse.of(null, UserResponse.from(saved));
    }

    public AuthResponse login(LoginRequest request) {
        log.info("Login attempt: email={}", request.email());

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.email(),
                        request.password()
                ));

        UserPrincipal principal= (UserPrincipal) authentication.getPrincipal();
        User user=principal.user();


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
