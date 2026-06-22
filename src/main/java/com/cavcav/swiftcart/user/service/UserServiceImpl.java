package com.cavcav.swiftcart.user.service;

import com.cavcav.swiftcart.common.exception.BusinessException;
import com.cavcav.swiftcart.common.response.PaginationResponse;
import com.cavcav.swiftcart.common.service.RateLimitService2;
import com.cavcav.swiftcart.user.dto.response.UserResponse;
import com.cavcav.swiftcart.user.model.Role;
import com.cavcav.swiftcart.user.model.User;
import com.cavcav.swiftcart.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;


@Service
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RateLimitService2 rateLimitService;

    public UserServiceImpl(UserRepository userRepository, RateLimitService2 rateLimitService) {
        this.userRepository = userRepository;
        this.rateLimitService = rateLimitService;
    }

    @Override
    public PaginationResponse<UserResponse> getUsers(int page, int size, String sortBy, String direction) {
        log.info("Fetching users: page={}, size={}, sortBy={}, direction={}", page, size, sortBy, direction);

        Sort sort = direction.equalsIgnoreCase("DESC")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        Page<User> userPage = userRepository.findAll(PageRequest.of(page, size, sort));

        log.info("Users fetched: total={}", userPage.getTotalElements());
        return PaginationResponse.of(userPage.map(UserResponse::from));
    }

    @Override
    public UserResponse getUserById(String id) {
        User user = userById(id);
        return UserResponse.from(user);
    }


    @Override
    public PaginationResponse<UserResponse> searchUsers(String email, int page, int size) {
        log.info("Searching users: email={}, page={}, size={}", email, page, size);
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<User> userPage = (email != null && !email.isBlank())
                ? userRepository.findByEmailContainingIgnoreCase(email, pageable)
                : userRepository.findAll(pageable);

        log.info("Search completed: total={}", userPage.getTotalElements());
        return PaginationResponse.of(userPage.map(UserResponse::from));
    }


    @Override
    @Transactional
    public UserResponse changeUserStatus(String id) {

        User user = userById(id);

        user.setIsActive(!user.getIsActive());

        User updatedUser = userRepository.save(user);

        log.info("User status changed. UserId={}, active={}",
                updatedUser.getId(),
                updatedUser.getIsActive());

        return UserResponse.from(updatedUser);
    }

    @Override
    public UserResponse updateUserRole(String id, Role role) {
        User user = userById(id);
        user.setRole(role);
        User updatedUser = userRepository.save(user);
        return UserResponse.from(updatedUser);
    }

    @Override
    public String deleteUserById(String id) {
        User user=userById(id);
        user.setIsActive(false);
        userRepository.save(user);
        return "User has been deleted";
    }


    private User userById(String userId) {
        return userRepository.findById(userId).orElseThrow(() -> {
            log.warn("User not found: id={}", userId);
            return new BusinessException("User not found", "USER_NOT_FOUND", HttpStatus.NOT_FOUND);
        });
    }
}
