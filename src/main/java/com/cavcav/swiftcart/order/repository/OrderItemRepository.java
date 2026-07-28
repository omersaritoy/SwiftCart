package com.cavcav.swiftcart.order.repository;

import com.cavcav.swiftcart.order.model.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemRepository extends JpaRepository<OrderItem, String> {
}
