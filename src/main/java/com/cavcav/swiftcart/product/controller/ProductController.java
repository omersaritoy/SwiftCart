package com.cavcav.swiftcart.product.controller;


import com.cavcav.swiftcart.auth.security.UserPrincipal;
import com.cavcav.swiftcart.common.response.ApiResponse;
import com.cavcav.swiftcart.product.dto.request.CreateProductRequest;
import com.cavcav.swiftcart.product.dto.response.ProductResponse;
import com.cavcav.swiftcart.product.repository.ProductRepository;
import com.cavcav.swiftcart.product.service.ProductService;
import com.cavcav.swiftcart.user.model.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

}
