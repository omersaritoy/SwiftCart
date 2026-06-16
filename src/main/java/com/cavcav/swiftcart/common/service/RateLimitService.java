package com.cavcav.swiftcart.common.service;

import com.cavcav.swiftcart.common.exception.RateLimitException;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.ConsumptionProbe;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@Service
@RequiredArgsConstructor
@Slf4j
public class RateLimitService {

    private final ProxyManager<String> proxyManager;



    public void checkLoginLimit(String email) {
        check("login:" + email, buildConfig(5, Duration.ofMinutes(1)));
    }

    public void checkSignupLimit(String ip) {
        check("signup:" + ip, buildConfig(3, Duration.ofHours(1)));
    }

    public void checkVerifyLimit(String token) {
        check("verify:" + token, buildConfig(3, Duration.ofMinutes(10)));
    }

    public void checkRefreshLimit(String email) {
        check("refresh:" + email, buildConfig(10, Duration.ofMinutes(1)));
    }



    private Bucket resolveBucket(String key, Supplier<BucketConfiguration> config) {
        return proxyManager.builder().build(key, config);
    }

    private BucketConfiguration buildConfig(int capacity, Duration duration) {
        Bandwidth limit = Bandwidth.builder()
                .capacity(capacity)
                .refillGreedy(capacity, duration)
                .build();

        return BucketConfiguration.builder()
                .addLimit(limit)
                .build();
    }

    private void check(String key, BucketConfiguration config) {
        Bucket bucket = resolveBucket(key, () -> config);
        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);

        if (!probe.isConsumed()) {
            long retryAfter = TimeUnit.NANOSECONDS.toSeconds(probe.getNanosToWaitForRefill());
            log.warn("Rate limit exceeded: key={}, retryAfter={}s", key, retryAfter);
            throw new RateLimitException(key, retryAfter, (int) bucket.getAvailableTokens());
        }

        log.debug("Rate limit passed: key={}, remaining={}", key, probe.getRemainingTokens());
    }
}