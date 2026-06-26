package com.cavcav.swiftcart.service;


import com.cavcav.swiftcart.auth.service.AuthService;
import com.cavcav.swiftcart.auth.service.JwtService;
import com.cavcav.swiftcart.common.service.RateLimitService2;
import com.cavcav.swiftcart.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mockito;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

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
        userRepository = Mockito.mock(UserRepository.class);
        applicationEventPublisher = Mockito.mock(ApplicationEventPublisher.class);
        passwordEncoder = Mockito.mock(PasswordEncoder.class);
        authenticationManager = Mockito.mock(AuthenticationManager.class);
        jwtService = Mockito.mock(JwtService.class);
        redisTemplate = Mockito.mock(RedisTemplate.class);
        rateLimitService = Mockito.mock(RateLimitService2.class);

    }

}
