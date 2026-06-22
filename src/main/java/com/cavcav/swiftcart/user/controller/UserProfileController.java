package com.cavcav.swiftcart.user.controller;

import com.cavcav.swiftcart.auth.security.UserPrincipal;
import com.cavcav.swiftcart.user.dto.request.CreateUserProfileRequest;
import com.cavcav.swiftcart.user.dto.response.UserProfileResponse;
import com.cavcav.swiftcart.user.service.UserProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

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
}
