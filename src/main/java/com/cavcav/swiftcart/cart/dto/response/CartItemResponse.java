package com.cavcav.swiftcart.cart.dto.response;

import com.cavcav.swiftcart.cart.model.CartItem;

import java.math.BigDecimal;

public record CartItemResponse(
        String id,
        String productId,
        String productName,
        String productImageUrl,
        Integer quantity,
        BigDecimal priceAtAddedTime,
        BigDecimal totalPrice
) {
    public static CartItemResponse from(CartItem item) {
        return new CartItemResponse(
                item.getId(),
                item.getProduct().getId(),
                item.getProduct().getName(),
                item.getProduct().getImageUrls() != null && !item.getProduct().getImageUrls().isEmpty()
                        ? item.getProduct().getImageUrls().getFirst()
                        : null,
                item.getQuantity(),
                item.getPriceAtAddedTime(),
                item.getPriceAtAddedTime().multiply(BigDecimal.valueOf(item.getQuantity()))

                );
    }
}
