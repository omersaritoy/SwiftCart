package com.cavcav.swiftcart.cart.service;

import com.cavcav.swiftcart.cart.Repository.CartRepository;
import com.cavcav.swiftcart.cart.dto.request.AddToCartRequest;
import com.cavcav.swiftcart.cart.dto.request.UpdateCartItemRequest;
import com.cavcav.swiftcart.cart.dto.response.CartResponse;
import com.cavcav.swiftcart.cart.model.Cart;
import com.cavcav.swiftcart.cart.model.CartItem;
import com.cavcav.swiftcart.common.exception.BusinessException;
import com.cavcav.swiftcart.product.model.Product;
import com.cavcav.swiftcart.product.repository.ProductRepository;
import com.cavcav.swiftcart.user.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CartServiceImpl implements CartService {

    private final ProductRepository productRepository;
    private final CartRepository cartRepository;


    @Override
    public CartResponse getMyCart(User user) {
        return null;
    }

    @Override
    @Transactional
    public CartResponse addToCart(AddToCartRequest request, User user) {
        log.info("Adding to cart: productId={}, userId={}", request.productId(), user.getId());

    }

    @Override
    public CartResponse updateCartItem(String cartItemId, UpdateCartItemRequest request, User user) {
        return null;
    }

    @Override
    public void removeCartItem(String cartItemId, User user) {

    }

    @Override
    public void clearCart(User user) {

    }
}
