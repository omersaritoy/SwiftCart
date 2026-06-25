package com.cavcav.swiftcart.user.dto.response;

import com.cavcav.swiftcart.user.model.Address;

public record AddressResponse(
        String id,
        String title,
        String fullAddress,
        String city,
        String district,
        String zipCode,
        String country,
        String phone,
        Boolean isDefault
) {
    public static AddressResponse from(Address address) {
        return new AddressResponse(
                address.getId(),
                address.getTitle(),
                address.getFullAddress(),
                address.getCity(),
                address.getDistrict(),
                address.getZipCode(),
                address.getCountry(),
                address.getPhone(),
                address.getIsDefault()
        );
    }
}
