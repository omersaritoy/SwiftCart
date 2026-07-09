package com.cavcav.swiftcart.cart.dto.response;

import com.cavcav.swiftcart.cart.model.Cart;

import java.math.BigDecimal;
import java.util.List;

public record CartResponse(
        String id,
        List<CartItemResponse> items,
        int totalItems,
        BigDecimal totalPrice
){
    public static CartResponse from(Cart cart) {
        List<CartItemResponse> items = cart.getItems()
                .stream()
                .map(CartItemResponse::from)
                .toList();

        BigDecimal totalPrice = items.stream()
                .map(CartItemResponse::totalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new CartResponse(
                cart.getId(),
                items,
                items.size(),
                totalPrice
        );
    }
}
