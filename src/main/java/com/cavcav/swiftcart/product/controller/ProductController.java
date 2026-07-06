package com.cavcav.swiftcart.product.controller;


import com.cavcav.swiftcart.auth.security.UserPrincipal;
import com.cavcav.swiftcart.common.response.ApiResponse;
import com.cavcav.swiftcart.common.response.PaginationResponse;
import com.cavcav.swiftcart.product.dto.request.CreateProductRequest;
import com.cavcav.swiftcart.product.dto.request.UpdateProductRequest;
import com.cavcav.swiftcart.product.dto.request.UpdateStockRequest;
import com.cavcav.swiftcart.product.dto.response.ProductResponse;
import com.cavcav.swiftcart.product.dto.response.ProductSummaryResponse;
import com.cavcav.swiftcart.product.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;


//POST   /api/v1/products                    (seller)
//PUT    /api/v1/products/{id}               (seller)
//DELETE /api/v1/products/{id}               (seller)
//PATCH  /api/v1/products/{id}/stock         (seller)
//PATCH  /api/v1/products/{id}/status        (seller)

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {
    private final ProductService productService;


    @PostMapping
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<ApiResponse<ProductResponse>> createProduct(@Valid @RequestBody CreateProductRequest request, @AuthenticationPrincipal UserPrincipal principal) {  // Authentication yerine direkt UserPrincipal
        return ResponseEntity.ok(ApiResponse.success(productService.createProduct(request, principal.user())));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PaginationResponse<ProductSummaryResponse>>> getProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String direction,
            @RequestParam(required = false) String categoryId) {
        return ResponseEntity.ok(ApiResponse.success(
                productService.getProducts(page, size, sortBy, direction, categoryId)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponse>> getProductById(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.success(productService.getProductById(id)));
    }

    @GetMapping("/seller/me")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<ApiResponse<PaginationResponse<ProductSummaryResponse>>> getMyProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success(
                productService.getMyProducts(page, size, principal.user())));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<ApiResponse<ProductResponse>> updateProduct(
            @PathVariable String id,
            @RequestBody UpdateProductRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success(
                productService.updateProduct(request, id, principal.user())));
    }

    @PatchMapping("/{id}/stock")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<ApiResponse<ProductResponse>> updateProductStock(@PathVariable String id, @RequestBody UpdateStockRequest request, @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success(productService.updateStockById(request, id, principal.user())));
    }

    @PatchMapping("{id}/status")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<ApiResponse<ProductResponse>> updateProductStatus(@PathVariable String id, @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success(productService.updateStatus(id, principal.user())));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteProduct(@PathVariable String id, @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success(productService.deleteProductById(id, principal.user())));
    }

}
