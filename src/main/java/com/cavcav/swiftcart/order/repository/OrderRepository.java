package com.cavcav.swiftcart.order.repository;

import com.cavcav.swiftcart.order.model.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order,String> {

}
