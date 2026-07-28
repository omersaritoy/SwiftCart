package com.cavcav.swiftcart.order.dto.response;

import com.cavcav.swiftcart.order.model.Order;
import com.cavcav.swiftcart.order.model.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record OrderSummaryResponse(
        String id,
        OrderStatus status,
        BigDecimal totalPrice,
        int itemCount,
        LocalDateTime createdAt
) {
    public static OrderSummaryResponse from(Order order) {
        return new OrderSummaryResponse(
                order.getId(),
                order.getStatus(),
                order.getTotalPrice(),
                order.getItems().size(),
                order.getCreatedAt()
        );
    }
}
