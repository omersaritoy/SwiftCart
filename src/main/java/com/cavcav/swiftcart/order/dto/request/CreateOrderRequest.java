package com.cavcav.swiftcart.order.dto.request;

import com.cavcav.swiftcart.order.model.OrderStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateOrderRequest(
        @NotBlank(message = "Address id is required")
        String addressId  // hangi adrese gönderilecek
) {
}
