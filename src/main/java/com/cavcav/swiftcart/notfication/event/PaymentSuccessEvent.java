package com.cavcav.swiftcart.notfication.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PaymentSuccessEvent(
        String paymentId,
        String orderId,
        String userId,
        String userEmail,
        BigDecimal amount,
        String transactionId,
        LocalDateTime createdAt
) {}

