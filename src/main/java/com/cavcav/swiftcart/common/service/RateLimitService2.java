package com.cavcav.swiftcart.common.service;

import com.cavcav.swiftcart.common.exception.RateLimitException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
@Slf4j
public class RateLimitService2 {

    private final RedisTemplate<String, Object> redisTemplate;

    public void checkLoginLimit(String email) {
        check("login:" + email, 5, Duration.ofMinutes(1));
    }

    public void checkSignupLimit(String ip) {
        check("signup:" + ip, 3, Duration.ofHours(1));
    }

    public void checkVerifyLimit(String token) {
        check("verify:" + token, 3, Duration.ofMinutes(10));
    }

    public void checkRefreshLimit(String email) {
        check("refresh:" + email, 10, Duration.ofMinutes(1));
    }

    public void checkGeneralLimit(String ip) {
        check("general:" + ip, 10, Duration.ofSeconds(30));
    }

    private void check(String key, int maxRequests, Duration window) {
        String redisKey = "rate_limiter:" + key;

        Long currentCount = redisTemplate.opsForValue().increment(redisKey);

        if (currentCount == 1) {
            redisTemplate.expire(redisKey, window);
        }

        if (currentCount > maxRequests) {
            Long ttl = redisTemplate.getExpire(redisKey);
            long retryAfter = (ttl != null && ttl > 0) ? ttl : window.getSeconds();
            log.warn("Rate limit exceeded: key={}, retryAfter={}s", key, retryAfter);
            throw new RateLimitException(key, retryAfter, maxRequests);
        }

        log.debug("Rate limit check passed: key={}, count={}/{}", key, currentCount, maxRequests);
    }
}