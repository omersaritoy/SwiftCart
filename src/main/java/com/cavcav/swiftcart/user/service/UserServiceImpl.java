package com.cavcav.swiftcart.user.service;

import com.cavcav.swiftcart.common.exception.BusinessException;
import com.cavcav.swiftcart.common.response.PaginationResponse;
import com.cavcav.swiftcart.user.dto.response.UserResponse;
import com.cavcav.swiftcart.user.model.User;
import com.cavcav.swiftcart.user.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;


@Service
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;


    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
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
        log.info("Fetching user: id={}", id);
        User user = userRepository.findById(id).orElseThrow(() -> {
            log.warn("User not found: id={}", id);
            return new BusinessException("User not found", "USER_NOT_FOUND", HttpStatus.NOT_FOUND);
        });

        return UserResponse.from(user);
    }

    @Override
    public UserResponse getUserByEmail(String email) {
        log.info("Fetching user: email={}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("User not found: email={}", email);
                    return new BusinessException("User not found", "USER_NOT_FOUND", HttpStatus.NOT_FOUND);
                });

        return UserResponse.from(user);
    }
}
