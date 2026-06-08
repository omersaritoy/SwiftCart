package com.cavcav.swiftcart.auth.dto.response;

import com.cavcav.swiftcart.user.dto.response.UserResponse;
import com.cavcav.swiftcart.user.model.User;

public record SignupResponse(
        String id,
        String email,
        String role,
        String message
) {
    public static SignupResponse from(User user) {
        return new SignupResponse(
                user.getId(),
                user.getEmail(),
                user.getRole().name(),
                "Registration successful. Please verify your email."
        );
    }
}

