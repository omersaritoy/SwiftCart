package com.cavcav.swiftcart.notfication.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record OrderCancelledEvent(
        String orderId,
        String userId,
        String userEmail,
        BigDecimal totalPrice,
        LocalDateTime cancelledAt
) {

}
