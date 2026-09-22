package com.cavcav.swiftcart.notfication.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderCreatedEvent(
        String orderId,
        String userId,
        String userEmail,
        BigDecimal totalPrice,
        List<String> productNames,
        LocalDateTime createdAt
) {}