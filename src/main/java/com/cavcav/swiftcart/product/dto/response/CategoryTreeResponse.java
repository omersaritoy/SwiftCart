package com.cavcav.swiftcart.product.dto.response;

import com.cavcav.swiftcart.product.model.Category;

import java.util.List;

public record CategoryTreeResponse(
        String id,
        String name,
        String description,
        List<CategoryTreeResponse> subCategories
) {
    public static CategoryTreeResponse from(Category category, List<Category> allCategories) {
        List<CategoryTreeResponse> subs = allCategories.stream()
                .filter(c -> c.getParentCategory() != null
                        && c.getParentCategory().getId().equals(category.getId()))
                .map(c -> CategoryTreeResponse.from(c, allCategories))
                .toList();

        return new CategoryTreeResponse(
                category.getId(),
                category.getName(),
                category.getDescription(),
                subs
        );
    }
}
