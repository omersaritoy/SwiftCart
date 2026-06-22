package com.cavcav.swiftcart.user.service;

import com.cavcav.swiftcart.common.response.PaginationResponse;
import com.cavcav.swiftcart.user.dto.response.UserResponse;
import com.cavcav.swiftcart.user.model.Role;

public interface UserService {

    PaginationResponse<UserResponse> getUsers(int page, int size, String sortBy, String direction);
    UserResponse getUserById(String id);
    PaginationResponse<UserResponse> searchUsers(String email, int page, int size);
    UserResponse changeUserStatus(String id);
    UserResponse updateUserRole(String id, Role role);
    String deleteUserById(String id);
}
