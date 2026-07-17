package com.cavcav.swiftcart.cart.controller;


import com.cavcav.swiftcart.auth.security.UserPrincipal;
import com.cavcav.swiftcart.cart.dto.request.AddToCartRequest;
import com.cavcav.swiftcart.cart.dto.response.CartResponse;
import com.cavcav.swiftcart.cart.service.CartService;
import com.cavcav.swiftcart.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/carts")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;


    @GetMapping("/me")
    public ResponseEntity<ApiResponse<CartResponse>> getMyCart(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success(cartService.getMyCart(principal.user())));
    }
    @PostMapping
    public ResponseEntity<ApiResponse<CartResponse>> addToCart(@RequestBody AddToCartRequest request,@AuthenticationPrincipal UserPrincipal principal){
        return ResponseEntity.ok(ApiResponse.success(cartService.addToCart(request,principal.user())));
    }

}
