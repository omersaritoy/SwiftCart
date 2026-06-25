package com.cavcav.swiftcart.user.dto.request;

import jakarta.validation.constraints.NotBlank;

public record CreateAddressRequest(
        @NotBlank(message = "Title is required")
        String title,

        @NotBlank(message = "Full address is required")
        String fullAddress,

        @NotBlank(message = "City is required")
        String city,

        @NotBlank(message = "District is required")
        String district,

        @NotBlank(message = "Zip code is required")
        String zipCode,

        @NotBlank(message = "Country is required")
        String country,

        @NotBlank(message = "Phone is required")
        String phone
) {}
