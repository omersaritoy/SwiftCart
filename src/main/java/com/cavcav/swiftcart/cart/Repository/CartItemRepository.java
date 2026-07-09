package com.cavcav.swiftcart.cart.Repository;

import com.cavcav.swiftcart.cart.model.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartItemRepository extends JpaRepository<CartItem,String > {
}
