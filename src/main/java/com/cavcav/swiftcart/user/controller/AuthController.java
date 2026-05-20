package com.cavcav.swiftcart.user.controller;

import com.cavcav.swiftcart.user.dto.request.LoginRequest;
import com.cavcav.swiftcart.user.dto.request.RegisterRequest;
import com.cavcav.swiftcart.user.dto.response.AuthResponse;
import com.cavcav.swiftcart.user.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.naming.AuthenticationException;

@RestController
@RequestMapping("/api/auth/v1")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping
    public ResponseEntity<AuthResponse> signup(@RequestBody RegisterRequest registerRequest) {
        return ResponseEntity.ok(authService.signup(registerRequest));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest loginRequest) throws AuthenticationException {
        return ResponseEntity.ok(authService.login(loginRequest));
    }
}
