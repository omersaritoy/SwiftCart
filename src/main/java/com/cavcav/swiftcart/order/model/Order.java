package com.cavcav.swiftcart.order.model;


import com.cavcav.swiftcart.user.model.BaseEntity;
import com.cavcav.swiftcart.user.model.User;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Order extends BaseEntity {


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="user_id",nullable = false)
    private User user;

    @OneToMany(mappedBy = "order",cascade = CascadeType.ALL,orphanRemoval = true)
    private List<OrderItem> items=new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalPrice;

    // Sipariş anındaki adres bilgisi — adres silinse bile korunur
    @Column(nullable = false)
    private String shippingAddress;


    @Column(nullable = false)
    private String shippingCity;

    @Column(nullable = false)
    private String shippingCountry;

    @Column(nullable = false)
    private String shippingZipCode;

    @Column(nullable = false)
    private String shippingPhone;

    private LocalDateTime cancelledAt;

}
