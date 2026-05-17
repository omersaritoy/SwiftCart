package com.cavcav.swiftcart.user.dto.response;

public record AuthResponse(
        String token,
        String tokenType,
        UserResponse user
) {
    public static AuthResponse of(String token, UserResponse user) {
        return new AuthResponse("Bearer " + token, "Bearer", user);
    }
}
