package com.cavcav.swiftcart.order.dto.response;


import com.cavcav.swiftcart.order.model.Order;
import com.cavcav.swiftcart.order.model.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderResponse(
        String id,
        String userId,
        List<OrderItemResponse> items,
        OrderStatus status,
        BigDecimal totalPrice,
        String shippingAddress,
        String shippingCity,
        String shippingCountry,
        String shippingZipCode,
        String shippingPhone,
        LocalDateTime createdAt,
        LocalDateTime cancelledAt
) {
    public static OrderResponse from(Order order) {
        return new OrderResponse(
                order.getId(),
                order.getUser().getId(),
                order.getItems().stream()
                        .map(OrderItemResponse::from)
                        .toList(),
                order.getStatus(),
                order.getTotalPrice(),
                order.getShippingAddress(),
                order.getShippingCity(),
                order.getShippingCountry(),
                order.getShippingZipCode(),
                order.getShippingPhone(),
                order.getCreatedAt(),
                order.getCancelledAt()
        );
    }
}

