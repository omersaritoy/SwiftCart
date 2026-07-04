package com.cavcav.swiftcart.product.service;

import com.cavcav.swiftcart.product.dto.request.CreateCategoryRequest;
import com.cavcav.swiftcart.product.dto.request.UpdateCategoryRequest;
import com.cavcav.swiftcart.product.dto.response.CategoryResponse;
import com.cavcav.swiftcart.product.dto.response.CategoryTreeResponse;
import com.cavcav.swiftcart.product.model.Category;

import java.util.List;

public interface CategoryService {
    CategoryResponse createCategory(CreateCategoryRequest request);
    List<CategoryResponse> getCategories();
    List<CategoryTreeResponse> getCategoryTree();
    CategoryResponse updateCategory(UpdateCategoryRequest request,String categoryId);
    String deleteCategory(String categoryId);
}
