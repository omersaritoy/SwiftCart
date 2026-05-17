package com.cavcav.swiftcart.user.dto.response;

import com.cavcav.swiftcart.user.model.User;

public record UserResponse(
        String id,
        String email,
        String role,
        Boolean isActive
) {
    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getRole().name(),
                user.getIsActive()
        );
    }
}
