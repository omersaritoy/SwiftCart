package com.cavcav.swiftcart.product.dto.response;

import com.cavcav.swiftcart.product.model.Category;
import com.cavcav.swiftcart.product.model.Product;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record CategoryResponse(
        String id,
        String name,
        String description,
        String parentCategoryId,
        String parentCategoryName
) {
    public static CategoryResponse from(Category category) {
        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getDescription(),
                category.getParentCategory() != null ? category.getParentCategory().getId() : null,
                category.getParentCategory() != null ? category.getParentCategory().getName() : null
        );
    }
}
