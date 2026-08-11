package com.cavcav.swiftcart.payment.dto.request;

import com.cavcav.swiftcart.payment.model.Payment;
import com.cavcav.swiftcart.payment.model.PaymentMethod;
import com.cavcav.swiftcart.payment.model.PaymentStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CreatePaymentRequest(
        @NotBlank(message = "Order id is required")
        String orderId,

        @NotNull(message = "Payment method is required")
        PaymentMethod method
) {}


