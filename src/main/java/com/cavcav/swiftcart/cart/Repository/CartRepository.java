package com.cavcav.swiftcart.cart.Repository;

import com.cavcav.swiftcart.cart.model.Cart;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart,String> {
    Optional<Cart> findByUserId(String userId);
}
