package com.cavcav.swiftcart.product.controller;

import com.cavcav.swiftcart.common.response.ApiResponse;
import com.cavcav.swiftcart.product.dto.request.CreateCategoryRequest;
import com.cavcav.swiftcart.product.dto.request.UpdateCategoryRequest;
import com.cavcav.swiftcart.product.dto.response.CategoryResponse;
import com.cavcav.swiftcart.product.dto.response.CategoryTreeResponse;
import com.cavcav.swiftcart.product.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
public class CategoryController {
    private final CategoryService categoryService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CategoryResponse>> createCategory(
            @Valid @RequestBody CreateCategoryRequest request) {
        return ResponseEntity.ok(ApiResponse.success(categoryService.createCategory(request)));
    }

    @GetMapping("/tree")
    public ResponseEntity<ApiResponse<List<CategoryTreeResponse>>> getCategoryTree() {
        return ResponseEntity.ok(ApiResponse.success(categoryService.getCategoryTree()));
    }
    @GetMapping
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getCategories() {
        return ResponseEntity.ok(ApiResponse.success(categoryService.getCategories()));
    }
    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CategoryResponse>> updateCategory(@RequestBody UpdateCategoryRequest request, @PathVariable String  id) {
        return ResponseEntity.ok(ApiResponse.success(categoryService.updateCategory(request,id)));
    }
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> deleteCategory(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.success(categoryService.deleteCategory(id)));
    }

}
