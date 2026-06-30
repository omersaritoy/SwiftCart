package com.cavcav.swiftcart.product.dto.response;

import com.cavcav.swiftcart.product.model.Product;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record ProductResponse(
        String id,
        String name,
        String description,
        BigDecimal price,
        Integer stock,
        String categoryId,
        String categoryName,
        String sellerId,
        List<String> imageUrls,
        Boolean isActive,
        LocalDateTime createdAt
) {
    public static ProductResponse from(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getStock(),
                product.getCategory().getId(),
                product.getCategory().getName(),
                product.getSeller().getId(),
                product.getImageUrls(),
                product.getIsActive(),
                product.getCreatedAt()
        );
    }
}
