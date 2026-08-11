package com.cavcav.swiftcart.payment.repository;


import com.cavcav.swiftcart.payment.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, String> {
    Optional<Payment> findByOrderId(String orderId);
    Optional<Payment> findByIdAndUserId(String id, String userId);
}