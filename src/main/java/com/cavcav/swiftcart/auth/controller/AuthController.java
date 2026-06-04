package com.cavcav.swiftcart.auth.controller;

import com.cavcav.swiftcart.common.response.ApiResponse;
import com.cavcav.swiftcart.notfication.service.EmailVerificationService;
import com.cavcav.swiftcart.auth.dto.request.LoginRequest;
import com.cavcav.swiftcart.auth.dto.request.RegisterRequest;
import com.cavcav.swiftcart.auth.dto.response.AuthResponse;
import com.cavcav.swiftcart.auth.service.AuthService;
import com.cavcav.swiftcart.user.dto.response.UserResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/v1/auth")
@CrossOrigin("*")
public class AuthController {

    private final AuthService authService;
    private final EmailVerificationService emailVerificationService;

    public AuthController(AuthService authService, EmailVerificationService emailVerificationService) {
        this.authService = authService;
        this.emailVerificationService = emailVerificationService;
    }

    @PostMapping
    public ResponseEntity<AuthResponse> signup(@RequestBody RegisterRequest registerRequest) {
        return ResponseEntity.ok(authService.signup(registerRequest));
    }


    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest loginRequest){
        return ResponseEntity.ok(authService.login(loginRequest));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<?>> logout(
            @RequestHeader("Authorization") String accessHeader,
            @RequestHeader("Refresh-Token") String refreshHeader) {
        authService.logout(accessHeader, refreshHeader);
        return ResponseEntity.ok(ApiResponse.success("Logged out successfully", null));
    }


    @GetMapping("/verify")
    public ResponseEntity<ApiResponse<UserResponse>> verify(@RequestParam String token) {
        return ResponseEntity.ok(ApiResponse.success(emailVerificationService.verify(token)));
    }
}
