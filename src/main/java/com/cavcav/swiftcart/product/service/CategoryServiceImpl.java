package com.cavcav.swiftcart.product.service;

import com.cavcav.swiftcart.common.exception.BusinessException;
import com.cavcav.swiftcart.product.dto.request.CreateCategoryRequest;
import com.cavcav.swiftcart.product.dto.response.CategoryResponse;
import com.cavcav.swiftcart.product.model.Category;
import com.cavcav.swiftcart.product.repository.CategoryRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

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
            parentCategory = categoryRepository.findById(request.parentCategoryId())
                    .orElseThrow(() -> {
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
                .parentCategory(parentCategory) // null olabilir, ana kategori demek
                .isActive(true)
                .build();
        Category saved = categoryRepository.save(category);
        log.info("Category created: id={}, name={}", saved.getId(), saved.getName());
        return CategoryResponse.from(saved);

    }


}
