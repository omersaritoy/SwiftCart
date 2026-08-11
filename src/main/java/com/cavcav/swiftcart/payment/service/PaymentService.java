package com.cavcav.swiftcart.payment.service;


import com.cavcav.swiftcart.common.exception.BusinessException;
import com.cavcav.swiftcart.notfication.service.EmailService;
import com.cavcav.swiftcart.order.model.Order;
import com.cavcav.swiftcart.order.model.OrderStatus;
import com.cavcav.swiftcart.order.repository.OrderRepository;
import com.cavcav.swiftcart.payment.dto.request.CreatePaymentRequest;
import com.cavcav.swiftcart.payment.dto.response.PaymentResponse;
import com.cavcav.swiftcart.payment.model.Payment;
import com.cavcav.swiftcart.payment.model.PaymentStatus;
import com.cavcav.swiftcart.payment.repository.PaymentRepository;
import com.cavcav.swiftcart.user.model.User;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final EmailService emailService;


    @Transactional
    public PaymentResponse processPayment(CreatePaymentRequest request, User user) {
        log.info("Processing payment: orderId={}, userId={}", request.orderId(), user.getId());

        Order order = orderRepository.findById(request.orderId()).orElseThrow(() -> {
            log.warn("Order not found: orderId={}", request.orderId());
            return new BusinessException(
                    "Order not found",
                    "ORDER_NOT_FOUND",
                    HttpStatus.NOT_FOUND
            );
        });
        if (!order.getUser().getId().equals(user.getId())) {
            log.warn("Unauthorized payment attempt: orderId={}, userId={}", request.orderId(), user.getId());
            throw new BusinessException(
                    "You are not authorized to pay for this order",
                    "PAYMENT_ACCESS_DENIED",
                    HttpStatus.FORBIDDEN
            );
        }
        if (order.getStatus() != OrderStatus.PENDING) {
            log.warn("Order already paid or cancelled: orderId={}, status={}", order.getId(), order.getStatus());
            throw new BusinessException(
                    "Order is not in PENDING status",
                    "ORDER_NOT_PENDING",
                    HttpStatus.BAD_REQUEST
            );
        }
        if (paymentRepository.findByOrderId(order.getId()).isPresent()) {
            log.warn("Payment already exists: orderId={}", order.getId());
            throw new BusinessException(
                    "Payment already exists for this order",
                    "PAYMENT_ALREADY_EXISTS",
                    HttpStatus.CONFLICT
            );
        }
        boolean isSuccess = Math.random() > 0.2;
        Payment payment = Payment.builder()
                .order(order)
                .user(user)
                .amount(order.getTotalPrice())
                .method(request.method())
                .build();
        if (isSuccess) {
            payment.setStatus(PaymentStatus.SUCCESS);
            payment.setTransactionId(UUID.randomUUID().toString());
            order.setStatus(OrderStatus.PAID);
            orderRepository.save(order);
            log.info("Payment successful: orderId={}, transactionId={}", order.getId(), payment.getTransactionId());
            emailService.sendPaymentSuccessEmail(user.getEmail(), order);
        } else {
            payment.setStatus(PaymentStatus.FAILED);
            payment.setFailureReason("Payment declined by bank");
            log.warn("Payment failed: orderId={}, userId={}", order.getId(), user.getId());
            emailService.sendPaymentFailedEmail(user.getEmail(), order);
        }
        Payment saved = paymentRepository.save(payment);
        return PaymentResponse.from(saved);
    }

    public PaymentResponse getPaymentById(String paymentId, User user) {
        log.info("Fetching payment: paymentId={}, userId={}", paymentId, user.getId());

        Payment payment = paymentRepository.findByIdAndUserId(paymentId, user.getId())
                .orElseThrow(() -> {
                    log.warn("Payment not found: paymentId={}, userId={}", paymentId, user.getId());
                    return new BusinessException(
                            "Payment not found",
                            "PAYMENT_NOT_FOUND",
                            HttpStatus.NOT_FOUND
                    );
                });

        return PaymentResponse.from(payment);
    }

    public PaymentResponse getPaymentByOrderId(String orderId, User user) {
        log.info("Fetching payment by order: orderId={}, userId={}", orderId, user.getId());

        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> {
                    log.warn("Payment not found for order: orderId={}", orderId);
                    return new BusinessException(
                            "Payment not found",
                            "PAYMENT_NOT_FOUND",
                            HttpStatus.NOT_FOUND
                    );
                });

        if (!payment.getUser().getId().equals(user.getId())) {
            log.warn("Unauthorized payment access: orderId={}, userId={}", orderId, user.getId());
            throw new BusinessException(
                    "You are not authorized to view this payment",
                    "PAYMENT_ACCESS_DENIED",
                    HttpStatus.FORBIDDEN
            );
        }

        return PaymentResponse.from(payment);
    }

    @Transactional
    public PaymentResponse refundPayment(String orderId, User user) {
        log.info("Refund request: orderId={}, userId={}", orderId, user.getId());

        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> {
                    log.warn("Payment not found for refund: orderId={}", orderId);
                    return new BusinessException(
                            "Payment not found",
                            "PAYMENT_NOT_FOUND",
                            HttpStatus.NOT_FOUND
                    );
                });
        if (!payment.getUser().getId().equals(user.getId())) {
            throw new BusinessException(
                    "You are not authorized to refund this payment",
                    "REFUND_ACCESS_DENIED",
                    HttpStatus.FORBIDDEN
            );
        }
        if (payment.getStatus() != PaymentStatus.SUCCESS) {
            log.warn("Cannot refund non-successful payment: paymentId={}, status={}",
                    payment.getId(), payment.getStatus());
            throw new BusinessException(
                    "Only successful payments can be refunded",
                    "PAYMENT_NOT_REFUNDABLE",
                    HttpStatus.BAD_REQUEST
            );
        }
        payment.setStatus(PaymentStatus.REFUNDED);
        Payment saved = paymentRepository.save(payment);

        log.info("Payment refunded: paymentId={}, orderId={}", payment.getId(), orderId);
        emailService.sendRefundEmail(user.getEmail(), payment.getOrder());

        return PaymentResponse.from(saved);
    }
}
