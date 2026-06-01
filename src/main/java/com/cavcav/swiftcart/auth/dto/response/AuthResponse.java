package com.cavcav.swiftcart.auth.dto.response;

import com.cavcav.swiftcart.user.dto.response.UserResponse;

public record AuthResponse(
        String token,
        String tokenType,
        UserResponse user
) {
    public static AuthResponse of(String token, UserResponse user) {
        return new AuthResponse("Bearer " + token, "Bearer", user);
    }
}
