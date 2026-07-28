package com.cavcav.swiftcart.order.dto.request;

import com.cavcav.swiftcart.order.model.OrderStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateOrderStatusRequest(
        @NotNull(message = "Status is required")
        OrderStatus status
) {}
