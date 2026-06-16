package com.cavcav.swiftcart.auth.controller;

import com.cavcav.swiftcart.auth.dto.response.LoginResponse;
import com.cavcav.swiftcart.auth.dto.response.SignupResponse;
import com.cavcav.swiftcart.common.response.ApiResponse;
import com.cavcav.swiftcart.notfication.service.EmailVerificationService;
import com.cavcav.swiftcart.auth.dto.request.LoginRequest;
import com.cavcav.swiftcart.auth.dto.request.RegisterRequest;
import com.cavcav.swiftcart.auth.service.AuthService;
import com.cavcav.swiftcart.user.dto.response.UserResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final EmailVerificationService emailVerificationService;

    @PostMapping
    public ResponseEntity<ApiResponse<SignupResponse>> signup(@Valid @RequestBody RegisterRequest request, HttpServletRequest httpRequest) {
        return ResponseEntity.ok(ApiResponse.success(
                authService.signup(request, getClientIp(httpRequest))));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(ApiResponse.success(authService.login(request)));
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

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<LoginResponse>> refresh(
            @RequestHeader("Refresh-Token") String refreshHeader) {
        return ResponseEntity.ok(ApiResponse.success(authService.refresh(refreshHeader)));
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isBlank()) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}
