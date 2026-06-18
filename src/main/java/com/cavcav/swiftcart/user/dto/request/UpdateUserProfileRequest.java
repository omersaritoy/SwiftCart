package com.cavcav.swiftcart.user.dto.request;

import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDate;

public record UpdateUserProfileRequest(
        String firstName,
        String lastName,

        @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Invalid phone number")
        String phone,

        @Past(message = "Birth date must be in the past")
        LocalDate birthDate
) {
}
