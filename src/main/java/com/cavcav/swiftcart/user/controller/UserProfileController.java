package com.cavcav.swiftcart.user.controller;

import com.cavcav.swiftcart.auth.security.UserPrincipal;
import com.cavcav.swiftcart.user.dto.request.CreateUserProfileRequest;
import com.cavcav.swiftcart.user.dto.request.UpdateUserProfileRequest;
import com.cavcav.swiftcart.user.dto.response.UserProfileResponse;
import com.cavcav.swiftcart.user.service.UserProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/profile")
@RequiredArgsConstructor
public class UserProfileController {
    private final UserProfileService userProfileService;


    @PostMapping
    public ResponseEntity<UserProfileResponse> createUserProfile(@RequestBody CreateUserProfileRequest request, Authentication authentication) {
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        return ResponseEntity.ok(userProfileService.createUserProfile(principal.getId(), request));
    }

    @GetMapping("/me")
    public ResponseEntity<UserProfileResponse> getCurrentUserProfile(Authentication authentication) {
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        return ResponseEntity.ok(userProfileService.getUserProfile(principal.getId()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserProfileResponse> getUserProfile(@PathVariable String id) {
        return ResponseEntity.ok(userProfileService.getUserProfile(id));
    }

    @PutMapping("/me")
    public ResponseEntity<UserProfileResponse> updateUserProfile(@RequestBody UpdateUserProfileRequest request, Authentication authentication) {
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        return ResponseEntity.ok(userProfileService.updateUserProfile(principal.getId(), request));
    }

    @PostMapping("/avatar")
    public ResponseEntity<UserProfileResponse> uploadAvatar(@RequestParam MultipartFile file, @AuthenticationPrincipal UserPrincipal userPrincipal) {
        return ResponseEntity.ok(userProfileService.uploadAvatar(
                        userPrincipal.getId(),
                        file
                )
        );
    }

    @DeleteMapping("/avatar")
    public ResponseEntity<String> deleteAvatar(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        userProfileService.deleteAvatar(userPrincipal.getId());
        return ResponseEntity.ok("Avatar deleted successfully");
    }
}
