package com.cavcav.swiftcart.product.dto.request;

import com.cavcav.swiftcart.product.model.Category;

public record UpdateCategoryRequest(
        String name,
        String description,
        String parentCategoryId
) {}
