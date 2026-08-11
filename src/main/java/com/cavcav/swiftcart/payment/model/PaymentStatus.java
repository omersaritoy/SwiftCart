package com.cavcav.swiftcart.payment.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

public enum PaymentStatus {
    PENDING,
    SUCCESS,
    FAILED,
    REFUNDED
}