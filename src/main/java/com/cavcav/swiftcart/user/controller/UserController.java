package com.cavcav.swiftcart.user.controller;

import com.cavcav.swiftcart.auth.security.UserPrincipal;
import com.cavcav.swiftcart.common.response.ApiResponse;
import com.cavcav.swiftcart.common.response.PaginationResponse;
import com.cavcav.swiftcart.user.dto.response.UserResponse;
import com.cavcav.swiftcart.user.model.Role;
import com.cavcav.swiftcart.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PaginationResponse<UserResponse>>> getUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String direction) {
        return ResponseEntity.ok(ApiResponse.success(
                userService.getUsers(page, size, sortBy, direction)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or #id == authentication.principal.id")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.success(userService.getUserById(id)));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getMyInfo(
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success(
                userService.getUserById(principal.getId())));
    }

    @GetMapping("/search")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PaginationResponse<UserResponse>>> searchUsers(
            @RequestParam(required = false) String email,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(
                userService.searchUsers(email, page, size)));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> updateUserStatus(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.success(userService.changeUserStatus(id)));
    }

    @PatchMapping("/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> updateUserRole(@PathVariable("id") String userId, @RequestParam Role role) {
        return ResponseEntity.ok(ApiResponse.success(userService.updateUserRole(userId, role)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') OR #id==authentication.principal.id")
    public ResponseEntity<ApiResponse<String>> deleteUser(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.success(userService.deleteUserById(id)));
    }

}
