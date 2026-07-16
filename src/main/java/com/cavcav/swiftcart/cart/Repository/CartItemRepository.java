package com.cavcav.swiftcart.cart.Repository;

import com.cavcav.swiftcart.cart.model.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CartItemRepository extends JpaRepository<CartItem,String > {
    List<CartItem> findAllByCartId(String cartId);
}
