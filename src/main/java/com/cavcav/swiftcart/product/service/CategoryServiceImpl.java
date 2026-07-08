package com.cavcav.swiftcart.product.service;

import com.cavcav.swiftcart.common.exception.BusinessException;
import com.cavcav.swiftcart.product.dto.request.CreateCategoryRequest;
import com.cavcav.swiftcart.product.dto.request.UpdateCategoryRequest;
import com.cavcav.swiftcart.product.dto.response.CategoryResponse;
import com.cavcav.swiftcart.product.dto.response.CategoryTreeResponse;
import com.cavcav.swiftcart.product.model.Category;
import com.cavcav.swiftcart.product.repository.CategoryRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    @Override
    @Transactional
    public CategoryResponse createCategory(CreateCategoryRequest request) {
        log.info("Creating category: name={}", request.name());
        Category parentCategory = null;

        if (request.parentCategoryId() != null && !request.parentCategoryId().isBlank()) {
            parentCategory = categoryRepository.findById(request.parentCategoryId()).orElseThrow(() -> {
                log.warn("Parent category not found: id={}", request.parentCategoryId());
                return new BusinessException(
                        "Parent category not found",
                        "PARENT_CATEGORY_NOT_FOUND",
                        HttpStatus.NOT_FOUND
                );
            });
        }
        Category category = Category.builder()
                .name(request.name())
                .description(request.description())
                .parentCategory(parentCategory)
                .isActive(true)
                .build();
        Category saved = categoryRepository.save(category);

        log.info("Category created: id={}, name={}", saved.getId(), saved.getName());
        return CategoryResponse.from(saved);


    }

    @Override
    public List<CategoryResponse> getCategories() {
        return categoryRepository.findAll().stream().map(CategoryResponse::from).toList();
    }

    @Override
    public List<CategoryTreeResponse> getCategoryTree() {

        List<Category> all = categoryRepository.findAll();
        return CategoryTreeResponse.buildTree(all);

    }

    @Override
    @Transactional
    public CategoryResponse updateCategory(UpdateCategoryRequest request, String categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new BusinessException(
                        "Category Not Found", "NOT_FOUND", HttpStatus.NOT_FOUND));

        if (request.name() != null) {
            category.setName(request.name());
        }
        if (request.description() != null) {
            category.setDescription(request.description());
        }
        if (request.parentCategoryId() != null) {
            Category parent = categoryRepository.findById(request.parentCategoryId())
                    .orElseThrow(() -> new BusinessException(
                            "Parent Category Not Found", "PARENT_NOT_FOUND", HttpStatus.NOT_FOUND));

            validateNoCycle(category, parent);
            category.setParentCategory(parent);
        }
        // Category updated = categoryRepository.save(category);
        return CategoryResponse.from(category);
    }

    @Override
    public String deleteCategory(String categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new BusinessException(
                        "Category Not Found", "NOT_FOUND", HttpStatus.NOT_FOUND));

        categoryRepository.delete(category);
        return "Category deleted";
    }

    @Override
    public CategoryResponse getCategoryById(String id) {
        Category category=categoryRepository.findById(id).orElseThrow(()->new BusinessException(
                "Category Not Found",
                "CATEGORY_NOT_FOUND",
                HttpStatus.NOT_FOUND
        ));

        return CategoryResponse.from(category);

    }

    private void validateNoCycle(Category category, Category newParent) {
        Category current = newParent;
        while (current != null) {
            if (current.getId().equals(category.getId())) {
                throw new BusinessException(
                        "Category cannot be a descendant of itself",
                        "INVALID_PARENT",
                        HttpStatus.BAD_REQUEST
                );
            }
            current = current.getParentCategory();
        }
    }


}
