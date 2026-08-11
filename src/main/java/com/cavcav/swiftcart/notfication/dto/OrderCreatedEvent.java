package com.cavcav.swiftcart.notfication.dto;

import com.cavcav.swiftcart.order.model.OrderStatus;

public record OrderCreatedEvent(String  orderId, String userEmail) {}

