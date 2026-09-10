package com.cavcav.swiftcart.order.dto.request;


import jakarta.validation.constraints.NotBlank;


public record CreateOrderRequest(
        @NotBlank(message = "Address id is required")
        String addressId  // hangi adrese gönderilecek
) {
}
