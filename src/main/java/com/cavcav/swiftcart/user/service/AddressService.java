package com.cavcav.swiftcart.user.service;


import com.cavcav.swiftcart.user.dto.request.CreateAddressRequest;
import com.cavcav.swiftcart.user.dto.request.UpdateAddressRequest;
import com.cavcav.swiftcart.user.dto.response.AddressResponse;
import com.cavcav.swiftcart.user.model.User;

import java.util.List;

public interface AddressService {
    List<AddressResponse> getMyAddresses(User currentUser);
    AddressResponse getAddressById(String id, User currentUser);
    AddressResponse createAddress(CreateAddressRequest request, User currentUser);
    AddressResponse updateAddress(String id, UpdateAddressRequest request, User currentUser);
    void deleteAddress(String id, User currentUser);
    AddressResponse setDefaultAddress(String id, User currentUser);
}
