package com.cavcav.swiftcart.cart.service;

import com.cavcav.swiftcart.cart.Repository.CartItemRepository;
import com.cavcav.swiftcart.cart.Repository.CartRepository;
import com.cavcav.swiftcart.cart.dto.request.AddToCartRequest;
import com.cavcav.swiftcart.cart.dto.request.UpdateCartItemRequest;
import com.cavcav.swiftcart.cart.dto.response.CartItemResponse;
import com.cavcav.swiftcart.cart.dto.response.CartResponse;
import com.cavcav.swiftcart.cart.model.Cart;
import com.cavcav.swiftcart.cart.model.CartItem;
import com.cavcav.swiftcart.common.exception.BusinessException;
import com.cavcav.swiftcart.product.model.Product;
import com.cavcav.swiftcart.product.repository.ProductRepository;
import com.cavcav.swiftcart.user.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.graphql.GraphQlProperties;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CartServiceImpl implements CartService {

    private final ProductRepository productRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;


    @Override
    public CartResponse getMyCart(User user) {
        Cart cart = cartRepository.findByUserId(user.getId()).orElseGet(() -> {
                    log.info("Creating new cart for user: userId={}", user.getId());
                    return Cart.builder()
                            .user(user)
                            .items(new ArrayList<>())
                            .build();
                }
        );

        return CartResponse.from(cart);
    }

    @Override
    @Transactional
    public CartResponse addToCart(AddToCartRequest request, User user) {
        log.info("Adding to cart: productId={}, userId={}", request.productId(), user.getId());
        Product product = productRepository.findById(request.productId()).orElseThrow(() -> {
            log.warn("Product not found with productId:{}", request.productId());
            return new BusinessException(
                    "Product Not Found ",
                    "PRODUCT_NOT_FOUND",

                    HttpStatus.NOT_FOUND);
        });

        if (product.getStock() < request.quantity()) {
            log.warn("Insufficient stock: productId={}, stock={}, requested={}",
                    product.getId(), product.getStock(), request.quantity());
            throw new BusinessException(
                    "Insufficient stock",
                    "INSUFFICIENT_STOCK",
                    HttpStatus.BAD_REQUEST
            );
        }
        Cart cart = cartRepository.findByUserId(user.getId())
                .orElseGet(() -> {
                    log.info("Creating new cart for user: userId={}", user.getId());
                    Cart newCart = Cart.builder()
                            .user(user)
                            .items(new ArrayList<>())
                            .build();
                    return cartRepository.save(newCart);
                });

        Optional<CartItem> existingItem = cart.getItems().stream()
                .filter(item -> item.getProduct().equals(request.productId())).findFirst();
        if (existingItem.isPresent()) {
            CartItem item = existingItem.get();
            int newQuantity = item.getQuantity() + request.quantity();
            if (product.getStock() < newQuantity)
                throw new BusinessException(
                        "Insufficient stock",
                        "INSUFFICIENT_STOCK",
                        HttpStatus.BAD_REQUEST
                );
            item.setQuantity(newQuantity);
            log.info("Cart item quantity updated: productId={}, newQuantity={}",
                    product.getId(), newQuantity);
        } else {
            CartItem newItem = CartItem.builder()
                    .cart(cart)
                    .product(product)
                    .quantity(request.quantity())
                    .priceAtAddedTime(product.getPrice())
                    .build();
            cart.getItems().add(newItem);
            log.info("New item added to cart: productId={}", product.getId());
        }
        Cart saved = cartRepository.save(cart);
        log.info("Cart updated: cartId={}, userId={}", saved.getId(), user.getId());
        return CartResponse.from(saved);


    }

    @Override
    public CartResponse updateCartItem(String cartItemId,
                                       UpdateCartItemRequest request,
                                       User user) {

        Cart cart = cartRepository.findByUserId(user.getId())
                .orElseThrow(() -> new BusinessException(
                        "Cart Not Found",
                        "CART_NOT_FOUND",
                        HttpStatus.NOT_FOUND));

        CartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new BusinessException(
                        "Item Not Found",
                        "ITEM_NOT_FOUND",
                        HttpStatus.NOT_FOUND));

        if (!cart.getItems().contains(item)) {
            throw new BusinessException(
                    "Item Not Found in Cart",
                    "ITEM_NOT_FOUND",
                    HttpStatus.NOT_FOUND);
        }

        if (request.quantity() <= 0) {
            throw new BusinessException(
                    "Quantity must be greater than zero.",
                    "INVALID_QUANTITY",
                    HttpStatus.BAD_REQUEST);
        }

        if (request.quantity() > item.getProduct().getStock()) {
            throw new BusinessException(
                    "Requested quantity exceeds available stock.",
                    "INSUFFICIENT_STOCK",
                    HttpStatus.BAD_REQUEST);
        }

        item.setQuantity(request.quantity());

        cartItemRepository.save(item);

        return CartResponse.from(cart);
    }

    @Override
    public void removeCartItem(String cartItemId, User user) {
        CartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new BusinessException(
                        "Item Not Found",
                        "ITEM_NOT_FOUND",
                        HttpStatus.NOT_FOUND));

        if (!item.getCart().getUser().getId().equals(user.getId())) {
            throw new BusinessException(
                    "Item Not Found",
                    "ITEM_NOT_FOUND",
                    HttpStatus.NOT_FOUND
            );
        }

        cartItemRepository.delete(item);
    }

    @Override
    @Transactional
    public void clearCart(User user) {
        log.info("Clearing cart: userId={}", user.getId());

        Cart cart = cartRepository.findByUserId(user.getId())
                .orElseThrow(() -> {
                    log.warn("Cart not found: userId={}", user.getId());
                    return new BusinessException(
                            "Cart not found",
                            "CART_NOT_FOUND",
                            HttpStatus.NOT_FOUND
                    );
                });
        cart.getItems().clear();
        cartRepository.save(cart);
        
        log.info("Cart cleared: cartId={}, userId={}", cart.getId(), user.getId());

    }
}
