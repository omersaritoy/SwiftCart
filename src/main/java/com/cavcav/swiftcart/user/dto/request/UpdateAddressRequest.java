package com.cavcav.swiftcart.user.dto.request;

public record UpdateAddressRequest(
        String title,
        String fullAddress,
        String city,
        String district,
        String zipCode,
        String country,
        String phone
) {}
