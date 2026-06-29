package com.cavcav.swiftcart.service;


import com.cavcav.swiftcart.auth.dto.UserRegisteredEvent;
import com.cavcav.swiftcart.auth.dto.request.RegisterRequest;
import com.cavcav.swiftcart.auth.dto.response.SignupResponse;
import com.cavcav.swiftcart.auth.service.AuthService;
import com.cavcav.swiftcart.auth.service.JwtService;
import com.cavcav.swiftcart.common.service.RateLimitService2;
import com.cavcav.swiftcart.user.model.User;
import com.cavcav.swiftcart.user.repository.UserRepository;
import com.sun.net.httpserver.HttpServer;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.mockito.ArgumentMatchers.any;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

public class AuthServiceTest {
    private UserRepository userRepository;
    private ApplicationEventPublisher applicationEventPublisher;
    private PasswordEncoder passwordEncoder;
    private AuthenticationManager authenticationManager;
    private JwtService jwtService;
    private RedisTemplate<String, String> redisTemplate;
    private RateLimitService2 rateLimitService;
    private AuthService authService;


    @BeforeEach
    public void setUp() {
        userRepository = mock(UserRepository.class);
        applicationEventPublisher = mock(ApplicationEventPublisher.class);
        passwordEncoder = mock(PasswordEncoder.class);
        authenticationManager = mock(AuthenticationManager.class);
        jwtService = mock(JwtService.class);
        redisTemplate = mock(RedisTemplate.class);
        rateLimitService = mock(RateLimitService2.class);
        authService = new AuthService(userRepository, applicationEventPublisher, passwordEncoder, authenticationManager, jwtService, redisTemplate, rateLimitService);
    }

    @Test
    @DisplayName("Should return SignupResponse when signup is successful")
    void shouldReturnSignupResponse() {
        RegisterRequest request = new RegisterRequest(
                "test@gmail.com",
                "password"
        );

        User savedUser = new User(
                "test@gmail.com",
                "encodedPassword"
        );
        savedUser.setId("user-123");

        when(userRepository.existsByEmail(request.email())).thenReturn(false);
        when(passwordEncoder.encode(request.password()))
                .thenReturn("encodedPassword");
        when(userRepository.save(Mockito.<User>any()))
                .thenReturn(savedUser);

        HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);
        when(httpServletRequest.getRemoteAddr())
                .thenReturn("127.0.0.1");

        SignupResponse response = authService.signup(request, httpServletRequest);
        // Assert
        assertNotNull(response);
        assertEquals("user-123", response.id());
        assertEquals("test@gmail.com", response.email());
        assertEquals(savedUser.getRole().name(), response.role());
        assertEquals(
                "Registration successful. Please verify your email.",
                response.message()
        );
        verify(rateLimitService).checkSignupLimit("127.0.0.1");
        verify(passwordEncoder).encode("password");
        verify(userRepository).existsByEmail("test@gmail.com");
        verify(userRepository).save(Mockito.<User>any());
        verify(applicationEventPublisher)
                .publishEvent(any(UserRegisteredEvent.class));

    }

}
