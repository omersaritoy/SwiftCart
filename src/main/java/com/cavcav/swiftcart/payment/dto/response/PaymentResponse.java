package com.cavcav.swiftcart.payment.dto.response;

import com.cavcav.swiftcart.payment.model.Payment;
import com.cavcav.swiftcart.payment.model.PaymentMethod;
import com.cavcav.swiftcart.payment.model.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PaymentResponse(
        String id,
        String orderId,
        String userId,
        BigDecimal amount,
        PaymentStatus status,
        PaymentMethod method,
        String transactionId,
        String failureReason,
        LocalDateTime createdAt
) {
        public static PaymentResponse from(Payment payment) {
                return new PaymentResponse(
                        payment.getId(),
                        payment.getOrder().getId(),
                        payment.getUser().getId(),
                        payment.getAmount(),
                        payment.getStatus(),
                        payment.getMethod(),
                        payment.getTransactionId(),
                        payment.getFailureReason(),
                        payment.getCreatedAt()
                );
        }
}
