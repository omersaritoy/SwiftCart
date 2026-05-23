package com.cavcav.swiftcart.notfication.service;

import com.cavcav.swiftcart.common.exception.BusinessException;
import com.cavcav.swiftcart.user.dto.response.UserResponse;
import com.cavcav.swiftcart.user.model.User;
import com.cavcav.swiftcart.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailVerificationService {

    private final UserRepository userRepository;
    private final RedisTemplate<String, String> redisTemplate;
    private final EmailService emailService;

    public void sendVerificationEmail(User user) {
        String token = generateToken();
        redisTemplate.opsForValue().set("verify:" + token, user.getId());
        emailService.sendVerificationEmail(user.getEmail(), token);
        log.info("Verification email sent: userId={}, email={}", user.getId(), user.getEmail());
    }

    public UserResponse verify(String token) {
        log.info("Email verification request: token={}", token);

        String userId = redisTemplate.opsForValue().get("verify:" + token);

        if (userId == null) {
            log.warn("Invalid or expired token: token={}", token);
            throw new BusinessException(
                    "Invalid or expired verification token",
                    "INVALID_TOKEN",
                    HttpStatus.BAD_REQUEST
            );
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("User not found: userId={}", userId);
                    return new BusinessException(
                            "User not found",
                            "USER_NOT_FOUND",
                            HttpStatus.NOT_FOUND
                    );
                });

        user.setIsActive(true);
        user.setIsEmailVerified(true);
        userRepository.save(user);
        redisTemplate.delete("verify:" + token);

        log.info("Email verified: userId={}, email={}", user.getId(), user.getEmail());
        return UserResponse.from(user);
    }

    private String generateToken() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
