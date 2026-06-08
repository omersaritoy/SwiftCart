package com.cavcav.swiftcart.auth.dto.response;

import com.cavcav.swiftcart.user.dto.response.UserResponse;

public record LoginResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        UserResponse user
) {
    public static LoginResponse of(String accessToken, String refreshToken, UserResponse user) {
        return new LoginResponse(accessToken, refreshToken, "Bearer", user);
    }
}
