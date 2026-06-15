package com.cavcav.swiftcart.common.exception;

import lombok.Getter;

@Getter
public class RateLimitException extends RuntimeException {

    private final String key;
    private final long retryAfter;
    private final int limit;

    public RateLimitException(String key, long retryAfter, int limit) {
        super("Rate limit exceeded");
        this.key = key;
        this.retryAfter = retryAfter;
        this.limit = limit;
    }
}
