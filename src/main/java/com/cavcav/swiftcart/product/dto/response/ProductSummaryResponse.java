package com.cavcav.swiftcart.product.dto.response;

import com.cavcav.swiftcart.product.model.Product;

import java.math.BigDecimal;

public record ProductSummaryResponse(
        String id,
        String name,
        BigDecimal price,
        String imageUrl,
        Boolean isActive
) {
    public static ProductSummaryResponse from(Product product) {
        return new ProductSummaryResponse(
                product.getId(),
                product.getName(),
                product.getPrice(),
                product.getImageUrls() != null && !product.getImageUrls().isEmpty()
                        ? product.getImageUrls().getFirst()
                        : null,
                product.getIsActive()
        );
    }
}
