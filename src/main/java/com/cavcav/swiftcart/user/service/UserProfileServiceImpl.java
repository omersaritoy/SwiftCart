package com.cavcav.swiftcart.user.service;

import com.cavcav.swiftcart.common.exception.BusinessException;
import com.cavcav.swiftcart.user.dto.request.CreateUserProfileRequest;
import com.cavcav.swiftcart.user.dto.request.UpdateUserProfileRequest;
import com.cavcav.swiftcart.user.dto.response.UserProfileResponse;
import com.cavcav.swiftcart.user.model.User;
import com.cavcav.swiftcart.user.model.UserProfile;
import com.cavcav.swiftcart.user.repository.UserProfileRepository;
import com.cavcav.swiftcart.user.repository.UserRepository;
import io.lettuce.core.KillArgs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import static io.lettuce.core.KillArgs.Builder.id;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserProfileServiceImpl implements UserProfileService {

    private final UserProfileRepository userProfileRepository;
    private final UserRepository userRepository;


    @Override
    public UserProfileResponse createUserProfile(
            String userId,
            CreateUserProfileRequest request) {

        if (userProfileRepository.existsByUserId(userId)) {
            throw new BusinessException(
                    "Profile Already exist",
                    "ALREADY_EXIST",
                    HttpStatus.CONFLICT
            );
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("User not found id:" + userId, "USER_NOT_FOUND", HttpStatus.NOT_FOUND));

        UserProfile profile = CreateUserProfileRequest.toEntity(request);
        profile.setUser(user);

        profile = userProfileRepository.save(profile);

        return UserProfileResponse.from(profile, userId);
    }

    @Override
    public UserProfileResponse getUserProfile(String userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new BusinessException("User not found id:" + userId, "USER_NOT_FOUND", HttpStatus.NOT_FOUND));
        UserProfile profile = userProfileRepository.getUserProfileByUser(user)
                .orElseThrow(() -> new BusinessException(
                        "Profile not found",
                        "PROFILE_NOT_FOUND",
                        HttpStatus.NOT_FOUND));


        return UserProfileResponse.from(profile, userId);
    }

    @Override
    public UserProfileResponse updateUserProfile(String profileId, UpdateUserProfileRequest request) {
        UserProfile profile = getUserProfileById(profileId);

        profile.setFirstName(request.firstName());
        profile.setLastName(request.lastName());
        profile.setPhone(request.phone());
        profile.setBirthDate(request.birthDate());

        UserProfile updated = userProfileRepository.save(profile);

        return UserProfileResponse.from(updated, updated.getUser().getId());
    }

    private UserProfile getUserProfileById(String profileId) {
        return userProfileRepository.findById(profileId).orElseThrow(() -> new BusinessException("User profile not found id:" + profileId, "USER_PROFILE_NOT_FOUND", HttpStatus.NOT_FOUND));
    }
}
