package com.cavcav.swiftcart.user.controller;

import com.cavcav.swiftcart.common.response.ApiResponse;
import com.cavcav.swiftcart.common.response.PaginationResponse;
import com.cavcav.swiftcart.user.dto.response.UserResponse;
import com.cavcav.swiftcart.user.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/user")
public class UserController {

    private final UserService  userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PaginationResponse<UserResponse>>> getUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String direction) {
        return ResponseEntity.ok(ApiResponse.success(userService.getUsers(page, size, sortBy, direction)));
    }
    @GetMapping("/")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@RequestParam(value = "id") String id) {
        return ResponseEntity.ok(ApiResponse.success(userService.getUserById(id)));
    }

    @GetMapping("/email/")
    public ResponseEntity<ApiResponse<UserResponse>> getUserByEmail(@RequestParam(value = "email") String email) {
        return ResponseEntity.ok(ApiResponse.success(userService.getUserByEmail(email)));
    }

}
