package com.cavcav.swiftcart.user.dto.request;

import com.cavcav.swiftcart.user.model.UserProfile;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDate;

public record CreateUserProfileRequest(
        @NotBlank(message = "First name is required")
        String firstName,

        @NotBlank(message = "Last name is required")
        String lastName,

        @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Invalid phone number")
        String phone,

        @Past(message = "Birth date must be in the past")
        LocalDate birthDate
) {
    public static UserProfile toEntity(CreateUserProfileRequest request) {
        UserProfile userProfile = new UserProfile();
        userProfile.setFirstName(request.firstName());
        userProfile.setLastName(request.lastName());
        userProfile.setPhone(request.phone());
        userProfile.setBirthDate(request.birthDate());
        return userProfile;
    }

}
