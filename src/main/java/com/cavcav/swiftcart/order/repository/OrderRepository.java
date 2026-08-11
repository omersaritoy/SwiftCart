package com.cavcav.swiftcart.order.repository;

import com.cavcav.swiftcart.order.model.Order;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface OrderRepository extends JpaRepository<Order,String> {

    Page<Order> findByUserId(String id, PageRequest of);

    @Query("select distinct o from  Order o join o.items i where i.product.seller.id =:sellerId ")
    Page<Order> findBySellerId(@Param("sellerId") String sellerId, Pageable pageable);

}
