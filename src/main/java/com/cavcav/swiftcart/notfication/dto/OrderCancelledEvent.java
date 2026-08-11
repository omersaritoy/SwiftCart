package com.cavcav.swiftcart.notfication.dto;

import com.cavcav.swiftcart.order.model.OrderStatus;

public record OrderCancelledEvent(String orderId, String userEmail) {
    public static record OrderStatusChangedEvent(String orderId, String userEmail, OrderStatus newStatus) {
    }
}
