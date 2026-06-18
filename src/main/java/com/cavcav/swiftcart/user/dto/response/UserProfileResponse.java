package com.cavcav.swiftcart.user.dto.response;

import com.cavcav.swiftcart.user.dto.request.CreateUserProfileRequest;
import com.cavcav.swiftcart.user.model.User;
import com.cavcav.swiftcart.user.model.UserProfile;

import java.time.LocalDate;

public record UserProfileResponse(
        String id,
        String userId,
        String firstName,
        String lastName,
        String phone,
        LocalDate birthDate,
        String avatarUrl
) {

    public static UserProfileResponse from(UserProfile request, String userId) {

        return new UserProfileResponse(
                request.getId(),
                userId,
                request.getFirstName(),
                request.getLastName(),
                request.getPhone(),
                request.getBirthDate(),
                request.getAvatarUrl()
        );
    }
}
