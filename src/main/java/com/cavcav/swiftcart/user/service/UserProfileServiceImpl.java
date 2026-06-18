package com.cavcav.swiftcart.user.service;

import com.cavcav.swiftcart.common.exception.BusinessException;
import com.cavcav.swiftcart.user.dto.request.CreateUserProfileRequest;
import com.cavcav.swiftcart.user.dto.response.UserProfileResponse;
import com.cavcav.swiftcart.user.model.User;
import com.cavcav.swiftcart.user.model.UserProfile;
import com.cavcav.swiftcart.user.repository.UserProfileRepository;
import com.cavcav.swiftcart.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

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

        return UserProfileResponse.from(profile,userId);
    }
}
