package com.cavcav.swiftcart.product.service;

import com.cavcav.swiftcart.product.dto.request.CreateCategoryRequest;
import com.cavcav.swiftcart.product.dto.response.CategoryResponse;
import com.cavcav.swiftcart.product.model.Category;

public interface CategoryService {
    CategoryResponse createCategory(CreateCategoryRequest request);

}
