package com.cavcav.swiftcart.user.service;

import com.cavcav.swiftcart.common.exception.BusinessException;
import com.cavcav.swiftcart.user.dto.request.CreateUserProfileRequest;
import com.cavcav.swiftcart.user.dto.request.UpdateUserProfileRequest;
import com.cavcav.swiftcart.user.dto.response.UserProfileResponse;
import com.cavcav.swiftcart.user.model.User;
import com.cavcav.swiftcart.user.model.UserProfile;
import com.cavcav.swiftcart.user.repository.UserProfileRepository;
import com.cavcav.swiftcart.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;


@Service
@Slf4j
@RequiredArgsConstructor
public class UserProfileServiceImpl implements UserProfileService {

    private final UserProfileRepository userProfileRepository;
    private final UserRepository userRepository;
    private final Path root= Paths.get("uploads/avatar");



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

    @Override
    public UserProfileResponse uploadAvatar(String userId,MultipartFile avatar) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(
                        "User not found id:" + userId,
                        "USER_NOT_FOUND",
                        HttpStatus.NOT_FOUND
                ));

        UserProfile profile = userProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new BusinessException(
                        "Profile not found",
                        "PROFILE_NOT_FOUND",
                        HttpStatus.NOT_FOUND
                ));

        try {
            if (!Files.exists(root)) {
                Files.createDirectories(root);
            }

            // eski avatarı sil
            if (profile.getAvatarUrl() != null) {
                String oldFileName = profile.getAvatarUrl()
                        .replace("/uploads/avatar/", "");
                Files.deleteIfExists(root.resolve(oldFileName));
            }

            String extension = getExtension(avatar);

            String fileName = UUID.randomUUID() + "." + extension;

            Files.copy(
                    avatar.getInputStream(),
                    root.resolve(fileName),
                    StandardCopyOption.REPLACE_EXISTING
            );

            String avatarUrl = "/uploads/avatar/" + fileName;

            profile.setAvatarUrl(avatarUrl);

            UserProfile saved = userProfileRepository.save(profile);

            return UserProfileResponse.from(saved, userId);

        } catch (IOException e) {
            throw new RuntimeException("Avatar upload failed", e);
        }
    }
    @Override
    public void deleteAvatar(String userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(
                        "User not found id:" + userId,
                        "USER_NOT_FOUND",
                        HttpStatus.NOT_FOUND
                ));

        UserProfile profile = userProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new BusinessException(
                        "Profile not found",
                        "PROFILE_NOT_FOUND",
                        HttpStatus.NOT_FOUND
                ));

        try {

            // avatar yoksa zaten çık
            if (profile.getAvatarUrl() == null) {
                return;
            }

            // dosya adını al
            String fileName = profile.getAvatarUrl()
                    .replace("/uploads/avatar/", "");

            // dosyayı sil
            Files.deleteIfExists(root.resolve(fileName));

            // DB'den temizle
            profile.setAvatarUrl(null);
            userProfileRepository.save(profile);

        } catch (IOException e) {
            throw new RuntimeException("Avatar delete failed", e);
        }
    }

    private String getExtension(MultipartFile avatar) {
        String fileName = avatar.getOriginalFilename();
        assert fileName != null;
        return fileName.substring(fileName.lastIndexOf("."));
    }

    private UserProfile getUserProfileById(String profileId) {
        return userProfileRepository.findById(profileId).orElseThrow(() -> new BusinessException("User profile not found id:" + profileId, "USER_PROFILE_NOT_FOUND", HttpStatus.NOT_FOUND));
    }
}
