package com.cavcav.swiftcart.user.service;


import com.cavcav.swiftcart.notfication.service.EmailService;
import com.cavcav.swiftcart.user.dto.request.LoginRequest;
import com.cavcav.swiftcart.user.dto.request.RegisterRequest;
import com.cavcav.swiftcart.user.dto.response.AuthResponse;
import com.cavcav.swiftcart.user.dto.response.UserResponse;
import com.cavcav.swiftcart.user.model.Role;
import com.cavcav.swiftcart.user.model.User;
import com.cavcav.swiftcart.user.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.naming.AuthenticationException;
import java.util.Random;

@Service
@Slf4j
public class AuthService {
    private final UserRepository userRepository;
    private final EmailService emailService;

    public AuthService(UserRepository userRepository, EmailService emailService) {
        this.userRepository = userRepository;
        this.emailService = emailService;
    }

    public AuthResponse signup(RegisterRequest registerRequest) {
        log.info("Signup request received for email: {}", registerRequest.email());
        User newUser = new User(registerRequest.email(), registerRequest.password());
        if(newUser.getRole().name().isBlank())
            newUser.setRole(Role.CUSTOMER);
        User savedUser = userRepository.save(newUser);
        emailService.sendVerificationEmail(savedUser.getEmail(),generateRandomToken());
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

    private String generateRandomToken() {
        Random random = new Random();
        int token = 100000 + random.nextInt(900000);
        return String.valueOf(token);
    }
}
