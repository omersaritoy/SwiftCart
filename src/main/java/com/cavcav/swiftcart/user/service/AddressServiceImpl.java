package com.cavcav.swiftcart.user.service;

import com.cavcav.swiftcart.common.exception.BusinessException;
import com.cavcav.swiftcart.user.dto.request.CreateAddressRequest;
import com.cavcav.swiftcart.user.dto.request.UpdateAddressRequest;
import com.cavcav.swiftcart.user.dto.response.AddressResponse;
import com.cavcav.swiftcart.user.model.Address;
import com.cavcav.swiftcart.user.model.User;
import com.cavcav.swiftcart.user.repository.AddressRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AddressServiceImpl implements AddressService {
    private final AddressRepository addressRepository;

    @Override
    public List<AddressResponse> getMyAddresses(User currentUser) {
        log.info("Fetching addresses: userId={}", currentUser.getId());

        List<Address> addresses = addressRepository.findByUserId(currentUser.getId());

        log.info("Addresses fetched: userId={}, total={}", currentUser.getId(), addresses.size());
        return addresses.stream().map(AddressResponse::from).toList();
    }
    @Override
    public AddressResponse getAddressById(String id, User currentUser) {
        log.info("Fetching address: id={}, userId={}", id, currentUser.getId());

        Address address = addressRepository.findByIdAndUserId(id, currentUser.getId())
                .orElseThrow(() -> {
                    log.warn("Address not found: id={}, userId={}", id, currentUser.getId());
                    return new BusinessException(
                            "Address not found",
                            "ADDRESS_NOT_FOUND",
                            HttpStatus.NOT_FOUND
                    );
                });

        return AddressResponse.from(address);
    }
    @Override
    @Transactional
    public AddressResponse createAddress(CreateAddressRequest request, User currentUser) {
        log.info("Creating address: userId={}", currentUser.getId());

        boolean isFirstAddress = addressRepository.findByUserId(currentUser.getId()).isEmpty();

        Address address = Address.builder()
                .user(currentUser)
                .title(request.title())
                .fullAddress(request.fullAddress())
                .city(request.city())
                .district(request.district())
                .zipCode(request.zipCode())
                .country(request.country())
                .phone(request.phone())
                .isDefault(isFirstAddress) // ilk adres otomatik varsayılan olsun
                .build();

        Address saved = addressRepository.save(address);

        log.info("Address created: id={}, userId={}", saved.getId(), currentUser.getId());
        return AddressResponse.from(saved);
    }
    @Override
    @Transactional
    public AddressResponse updateAddress(String id, UpdateAddressRequest request, User currentUser) {
        log.info("Updating address: id={}, userId={}", id, currentUser.getId());

        Address address = addressRepository.findByIdAndUserId(id, currentUser.getId())
                .orElseThrow(() -> {
                    log.warn("Address not found for update: id={}, userId={}", id, currentUser.getId());
                    return new BusinessException(
                            "Address not found",
                            "ADDRESS_NOT_FOUND",
                            HttpStatus.NOT_FOUND
                    );
                });

        if (request.title() != null) address.setTitle(request.title());
        if (request.fullAddress() != null) address.setFullAddress(request.fullAddress());
        if (request.city() != null) address.setCity(request.city());
        if (request.district() != null) address.setDistrict(request.district());
        if (request.zipCode() != null) address.setZipCode(request.zipCode());
        if (request.country() != null) address.setCountry(request.country());
        if (request.phone() != null) address.setPhone(request.phone());

        Address updated = addressRepository.save(address);

        log.info("Address updated: id={}, userId={}", id, currentUser.getId());
        return AddressResponse.from(updated);
    }
    @Override
    @Transactional
    public void deleteAddress(String id, User currentUser) {
        log.info("Deleting address: id={}, userId={}", id, currentUser.getId());

        Address address = addressRepository.findByIdAndUserId(id, currentUser.getId())
                .orElseThrow(() -> {
                    log.warn("Address not found for deletion: id={}, userId={}", id, currentUser.getId());
                    return new BusinessException(
                            "Address not found",
                            "ADDRESS_NOT_FOUND",
                            HttpStatus.NOT_FOUND
                    );
                });

        boolean wasDefault = address.getIsDefault();
        addressRepository.delete(address);

        // silinen adres varsayılansa, başka bir adresi varsayılan yap
        if (wasDefault) {
            List<Address> remaining = addressRepository.findByUserId(currentUser.getId());
            if (!remaining.isEmpty()) {
                Address newDefault = remaining.get(0);
                newDefault.setIsDefault(true);
                addressRepository.save(newDefault);
                log.info("New default address set: id={}, userId={}",
                        newDefault.getId(), currentUser.getId());
            }
        }

        log.info("Address deleted: id={}, userId={}", id, currentUser.getId());
    }
    @Override
    @Transactional
    public AddressResponse setDefaultAddress(String id, User currentUser) {
        log.info("Setting default address: id={}, userId={}", id, currentUser.getId());

        Address address = addressRepository.findByIdAndUserId(id, currentUser.getId())
                .orElseThrow(() -> {
                    log.warn("Address not found: id={}, userId={}", id, currentUser.getId());
                    return new BusinessException(
                            "Address not found",
                            "ADDRESS_NOT_FOUND",
                            HttpStatus.NOT_FOUND
                    );
                });

        addressRepository.clearDefaultForUser(currentUser.getId());

        address.setIsDefault(true);
        Address updated = addressRepository.save(address);

        log.info("Default address set: id={}, userId={}", id, currentUser.getId());
        return AddressResponse.from(updated);
    }

}
