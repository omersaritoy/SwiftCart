package com.cavcav.swiftcart.cart.service;

import com.cavcav.swiftcart.cart.dto.request.AddToCartRequest;
import com.cavcav.swiftcart.cart.dto.request.UpdateCartItemRequest;
import com.cavcav.swiftcart.cart.dto.response.CartResponse;
import com.cavcav.swiftcart.user.model.User;

public interface CartService {
    CartResponse getMyCart(User user);

    CartResponse addToCart(AddToCartRequest request, User user);

    CartResponse updateCartItem(String cartItemId, UpdateCartItemRequest request, User user);

    void removeCartItem(String cartItemId, User user);

    void clearCart(User user);

}
