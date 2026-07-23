package com.cavcav.swiftcart.order.model;

public enum OrderStatus {
    PENDING,       // ödeme bekleniyor
    PAID,          // ödendi
    PROCESSING,    // hazırlanıyor
    SHIPPED,       // kargoya verildi
    DELIVERED,     // teslim edildi
    CANCELLED      // iptal edildi
}