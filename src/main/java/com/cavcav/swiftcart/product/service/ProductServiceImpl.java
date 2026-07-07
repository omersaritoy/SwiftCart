package com.cavcav.swiftcart.product.service;

import com.cavcav.swiftcart.common.exception.BusinessException;
import com.cavcav.swiftcart.common.response.PaginationResponse;
import com.cavcav.swiftcart.product.dto.request.CreateProductRequest;
import com.cavcav.swiftcart.product.dto.request.UpdateProductRequest;
import com.cavcav.swiftcart.product.dto.request.UpdateStockRequest;
import com.cavcav.swiftcart.product.dto.response.ProductResponse;
import com.cavcav.swiftcart.product.dto.response.ProductSummaryResponse;
import com.cavcav.swiftcart.product.model.Category;
import com.cavcav.swiftcart.product.model.Product;
import com.cavcav.swiftcart.product.repository.CategoryRepository;
import com.cavcav.swiftcart.product.repository.ProductRepository;
import com.cavcav.swiftcart.user.model.Role;
import com.cavcav.swiftcart.user.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;


@Service
@Slf4j
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    @Override
    public ProductResponse createProduct(CreateProductRequest request, User seller) {
        log.info("Create product request: {}", request);
        Category category = findCategoryById(request.categoryId());
        isSeller(seller, "User is not a seller: userId={}, role={}");

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


    @Override
    @Transactional(readOnly = true)
    public PaginationResponse<ProductSummaryResponse> getProducts(int page, int size, String sortBy, String direction, String categoryId) {

        log.info("Fetching products: page={}, size={}, sortBy={}, direction={}, categoryId={}",
                page, size, sortBy, direction, categoryId);

        Sort sort = direction.equalsIgnoreCase("DESC")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Product> productPage = (categoryId != null && !categoryId.isBlank())
                ? productRepository.findByCategoryIdAndIsActiveTrue(categoryId, pageable)
                : productRepository.findByIsActiveTrue(pageable);

        log.info("Products fetched: total={}", productPage.getTotalElements());
        return PaginationResponse.of(productPage.map(ProductSummaryResponse::from));

    }

    @Override
    public ProductResponse getProductById(String id) {
        log.info("Fetching product id={}", id);

        Product product = findProductById(id);
        return ProductResponse.from(product);
    }

    @Override
    @Transactional(readOnly = true)
    public PaginationResponse<ProductSummaryResponse> getMyProducts(int page, int size, User seller) {
        if (!seller.getRole().equals(Role.SELLER))
            throw new BusinessException("User not a seller", "SELLER_REQUIRED", HttpStatus.FORBIDDEN);
        log.info("Fetching seller products: sellerId={}, page={}, size={}",
                seller.getId(), page, size);

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Product> productPage = productRepository.findBySellerId(seller.getId(), pageable);

        log.info("Seller products fetched: sellerId={}, total={}",
                seller.getId(), productPage.getTotalElements());

        return PaginationResponse.of(productPage.map(ProductSummaryResponse::from));
    }

    @Override
    public ProductResponse updateProduct(UpdateProductRequest request, String productId, User seller) {

        Product product = findProductById(productId);
        Category category = findCategoryById(request.categoryId());
        isSeller(seller, "User not a seller: userId={}, role={}");

        isOwner(seller, product);

        if (!request.name().isBlank()) {
            product.setName(request.name());
        }
        if (!request.description().isBlank()) {
            product.setDescription(request.description());
        }
        if (request.price() != null)
            product.setPrice(request.price());
        if (!request.categoryId().isBlank())
            product.setCategory(category);

        productRepository.save(product);
        return ProductResponse.from(product);

    }

    @Override
    public String deleteProductById(String id, User seller) {
        log.warn("Deleting product id={}", id);
        Product product = findProductById(id);
        isSeller(seller, "User not a seller: userId={}, role={}");
        isOwner(seller, product);
        productRepository.delete(product);
        return "Product Deleted Successfully";
    }

    @Override
    public ProductResponse updateStockById(UpdateStockRequest request, String productId, User seller) {
        Product product = findProductById(productId);
        isSeller(seller, "User not a seller: userId={}, role={}");
        product.setStock(request.stock());
        isOwner(seller, product);
        Product updatedProduct = productRepository.save(product);
        return ProductResponse.from(updatedProduct);
    }

    @Override
    public ProductResponse updateStatus(String productId, User seller) {
        Product product = findProductById(productId);
        isSeller(seller, "User not a seller: userId={}, role={}");
        isOwner(seller, product);
        product.setIsActive(!product.getIsActive());
        Product updatedProduct = productRepository.save(product);
        return ProductResponse.from(updatedProduct);
    }

    private static void isOwner(User seller, Product product) {
        if (!product.getSeller().getId().equals(seller.getId()))
            throw new BusinessException(
                    "Seller does not own this product.",
                    "PRODUCT_NOT_OWNED_BY_SELLER",
                    HttpStatus.FORBIDDEN
            );
    }

    private Product findProductById(String id) {
        return productRepository.findById(id).orElseThrow(() -> {
            log.warn("Product not found: id={}", id);
            return new BusinessException(
                    "Product not found",
                    "PRODUCT_NOT_FOUND",
                    HttpStatus.NOT_FOUND
            );
        });
    }

    private static void isSeller(User seller, String format) {
        if (!seller.getRole().equals(Role.SELLER)) {
            log.warn(format, seller.getId(), seller.getRole());

            throw new BusinessException(
                    "Only sellers can create products",
                    "SELLER_REQUIRED",
                    HttpStatus.FORBIDDEN
            );
        }
    }

    private Category findCategoryById(String request) {
        return categoryRepository.findById(request)
                .orElseThrow(() -> {
                    log.warn("Category not found: id={}", request);
                    return new BusinessException(
                            "Category not found",
                            "CATEGORY_NOT_FOUND",
                            HttpStatus.NOT_FOUND
                    );
                });
    }

}
