package com.cavcav.swiftcart.user.service;

import com.cavcav.swiftcart.user.dto.request.CreateUserProfileRequest;
import com.cavcav.swiftcart.user.dto.request.UpdateUserProfileRequest;
import com.cavcav.swiftcart.user.dto.response.UserProfileResponse;
import com.cavcav.swiftcart.user.model.UserProfile;
import org.springframework.web.multipart.MultipartFile;

public interface UserProfileService {

    UserProfileResponse createUserProfile(String userId,CreateUserProfileRequest request);
    UserProfileResponse getUserProfile(String userId);
    UserProfileResponse updateUserProfile(String profileId, UpdateUserProfileRequest request);
    UserProfileResponse uploadAvatar(String userId,MultipartFile avatar);
    void deleteAvatar(String userId);
}
