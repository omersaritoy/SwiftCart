package com.cavcav.swiftcart.user.dto.response;

import com.cavcav.swiftcart.user.model.User;
import com.cavcav.swiftcart.user.model.UserProfile;

import java.time.LocalDate;

public record UserResponse(
        String id,
        String email,
        String role,
        Boolean isActive,
        Boolean isEmailVerified
) {
    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getRole().name(),
                user.getIsActive(),
                user.getIsEmailVerified()
        );
    }
}
