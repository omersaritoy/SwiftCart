package com.cavcav.swiftcart.order.repository;

import com.cavcav.swiftcart.order.model.OrderItem;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface OrderItemRepository extends JpaRepository<OrderItem, String> {
    @Query("SELECT count(oi)>0 from OrderItem oi where oi.order.user.id=:userId " +
            "and oi.product.id=:productId and oi.order.status='DELIVERED'")
    boolean hasUserPurchasedProduct(@Param("userId") String userId,@Param("productId") String productId);
}
