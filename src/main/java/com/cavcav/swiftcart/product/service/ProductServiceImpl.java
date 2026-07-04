package com.cavcav.swiftcart.product.service;

import com.cavcav.swiftcart.common.exception.BusinessException;
import com.cavcav.swiftcart.product.dto.request.CreateProductRequest;
import com.cavcav.swiftcart.product.dto.response.ProductResponse;
import com.cavcav.swiftcart.product.model.Category;
import com.cavcav.swiftcart.product.model.Product;
import com.cavcav.swiftcart.product.repository.CategoryRepository;
import com.cavcav.swiftcart.product.repository.ProductRepository;
import com.cavcav.swiftcart.user.model.Role;
import com.cavcav.swiftcart.user.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
//GET    /api/v1/categories
//POST   /api/v1/categories                  (admin)
//PUT    /api/v1/categories/{id}             (admin)
//DELETE /api/v1/categories/{id}             (admin)
//
//GET    /api/v1/products
//GET    /api/v1/products/{id}
//GET    /api/v1/products/seller/me          (seller)
//POST   /api/v1/products                    (seller)
//PUT    /api/v1/products/{id}               (seller)
//DELETE /api/v1/products/{id}               (seller)
//PATCH  /api/v1/products/{id}/stock         (seller)
//PATCH  /api/v1/products/{id}/status        (seller)

@Service
@Slf4j
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    @Override
    public ProductResponse createProduct(CreateProductRequest request, User seller) {
        log.info("Create product request: {}", request);
        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> {
                    log.warn("Category not found: id={}", request.categoryId());
                    return new BusinessException(
                            "Category not found",
                            "CATEGORY_NOT_FOUND",
                            HttpStatus.NOT_FOUND
                    );
                });
        if (!seller.getRole().equals(Role.SELLER)) {
            log.warn("User is not a seller: userId={}, role={}", seller.getId(), seller.getRole());

            throw new BusinessException(
                    "Only sellers can create products",
                    "SELLER_REQUIRED",
                    HttpStatus.FORBIDDEN
            );
        }

        Product product = Product.builder()
                .seller(seller)
                .name(request.name())
                .description(request.description())
                .price(request.price())
                .stock(request.stock())
                .isActive(true)
                .imageUrls(new ArrayList<>())
                .category(category).build();
        Product savedProduct = productRepository.save(product);
        log.info("Product created: id={}, sellerId={}", savedProduct.getId(), seller.getId());
        return ProductResponse.from(savedProduct);
    }
}
