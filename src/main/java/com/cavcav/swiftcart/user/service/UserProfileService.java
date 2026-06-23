package com.cavcav.swiftcart.user.service;

import com.cavcav.swiftcart.user.dto.request.CreateUserProfileRequest;
import com.cavcav.swiftcart.user.dto.request.UpdateUserProfileRequest;
import com.cavcav.swiftcart.user.dto.response.UserProfileResponse;
import com.cavcav.swiftcart.user.model.UserProfile;

public interface UserProfileService {

    UserProfileResponse createUserProfile(String userId,CreateUserProfileRequest request);
    UserProfileResponse getUserProfile(String userId);
    UserProfileResponse updateUserProfile(String profileId, UpdateUserProfileRequest request);

}
