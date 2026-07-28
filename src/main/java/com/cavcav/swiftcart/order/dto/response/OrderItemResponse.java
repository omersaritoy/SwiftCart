package com.cavcav.swiftcart.order.dto.response;

import com.cavcav.swiftcart.order.model.OrderItem;

import java.math.BigDecimal;

public record OrderItemResponse(
        String id,
        String productId,
        String productName,
        Integer quantity,
        BigDecimal priceAtOrder,
        BigDecimal totalPrice
) {
    public static OrderItemResponse from(OrderItem item) {
        return new OrderItemResponse(
                item.getId(),
                item.getProduct().getId(),
                item.getProductName(),
                item.getQuantity(),
                item.getPriceAtOrder(),
                item.getTotalPrice()
        );
    }
}
