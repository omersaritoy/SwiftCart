package com.cavcav.swiftcart.notfication.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PaymentFailedEvent(
        String paymentId,
        String orderId,
        String userId,
        String userEmail,
        BigDecimal amount,
        String failureReason,
        LocalDateTime createdAt
) {}
