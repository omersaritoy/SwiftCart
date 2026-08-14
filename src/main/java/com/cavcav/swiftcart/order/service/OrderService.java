package com.cavcav.swiftcart.order.service;

import com.cavcav.swiftcart.cart.Repository.CartRepository;
import com.cavcav.swiftcart.cart.model.Cart;
import com.cavcav.swiftcart.cart.model.CartItem;
import com.cavcav.swiftcart.common.exception.BusinessException;
import com.cavcav.swiftcart.common.response.PaginationResponse;
import com.cavcav.swiftcart.notfication.service.EmailService;
import com.cavcav.swiftcart.order.dto.request.CreateOrderRequest;
import com.cavcav.swiftcart.order.dto.response.OrderResponse;
import com.cavcav.swiftcart.order.dto.response.OrderSummaryResponse;
import com.cavcav.swiftcart.order.model.Order;
import com.cavcav.swiftcart.order.model.OrderItem;
import com.cavcav.swiftcart.order.model.OrderStatus;
import com.cavcav.swiftcart.order.repository.OrderRepository;
import com.cavcav.swiftcart.product.model.Product;
import com.cavcav.swiftcart.product.repository.ProductRepository;
import com.cavcav.swiftcart.user.model.Address;
import com.cavcav.swiftcart.user.model.User;
import com.cavcav.swiftcart.user.repository.AddressRepository;

import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;

@RequiredArgsConstructor
@Slf4j
@Service
public class OrderService {
    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final AddressRepository addressRepository;
    private final ProductRepository productRepository;
    private final EmailService emailService;

    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request, User user) {
        log.info("Creating order: userId={}, addressId={}", user.getId(), request.addressId());
        Cart cart = getValidCart(user.getId());
        Address address = getValidAddress(request.addressId(), user.getId());


        Order order = builderOrder(user, address);
        BigDecimal orderTotal = attachOrderItemsAndDeductStock(order, cart);
        order.setTotalPrice(orderTotal);

        Order savedOrder = orderRepository.save(order);
        log.info("Order created: orderId={}, userId={}, totalPrice={}, itemCount={}",
                savedOrder.getId(), user.getId(), savedOrder.getTotalPrice(), savedOrder.getItems().size());

        cart.getItems().clear();
        cartRepository.save(cart);

        emailService.sendOrderConfirmationEmail(user.getEmail(), savedOrder);
        log.info("Order confirmation email sent: orderId={}, userId={}", savedOrder.getId(), user.getId());

        return OrderResponse.from(savedOrder);
    }

    @Transactional(readOnly = true)
    public PaginationResponse<OrderSummaryResponse> getMyOrders(int page, int size, String sortBy, String direction, User user) {
        log.info("Get my orders request: userId={}, page={}, size={}, sortBy={}, direction={}",
                user.getId(), page, size, sortBy, direction);

        Sort sort=resolveSort(String sortBy,String direction);

    }
    private Sort resolveSort(String sortBy, String direction) {
        if (!ALLOWED_ORDER_SORT_FIELDS.contains(sortBy)) {
            log.warn("Invalid sort field requested: sortBy={}", sortBy);
            throw new BusinessException("Invalid Sort Field", "INVALID_SORT_FIELD", HttpStatus.BAD_REQUEST);
        }
        return direction.equalsIgnoreCase("DESC")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
    }



    private BigDecimal attachOrderItemsAndDeductStock(Order order, Cart cart) {
        BigDecimal orderTotal = BigDecimal.ZERO;

        for (CartItem item : cart.getItems()) {
            Product product = item.getProduct();
            int quantity = item.getQuantity();
            int updatedRows = productRepository.decreaseStock(product.getId(), quantity);
            if (updatedRows == 0) {
                log.warn("Insufficient stock: productId={}, requested={}", product.getId(), quantity);
                throw new BusinessException("Quantity Bigger Than Stock", "QUANTITY_NEED_DOWN", HttpStatus.BAD_REQUEST);
            }
            BigDecimal itemTotal = product.getPrice().multiply(BigDecimal.valueOf(quantity));
            order.getItems().add(OrderItem.builder()
                    .order(order)
                    .product(product)
                    .productName(product.getName())
                    .priceAtOrder(item.getPriceAtAddedTime())
                    .quantity(quantity)
                    .totalPrice(itemTotal)
                    .build());
            orderTotal = orderTotal.add(itemTotal);

        }
        return orderTotal;
    }

    private Order builderOrder(User user, Address address) {
        return Order.builder()
                .user(user)
                .status(OrderStatus.PENDING)
                .shippingAddress(address.getTitle())
                .shippingCity(address.getCity())
                .shippingCountry(address.getCountry())
                .shippingZipCode(address.getZipCode())
                .shippingPhone(address.getPhone())
                .items(new ArrayList<>())
                .build();
    }

    private Address getValidAddress(@NotBlank(message = "Address id is required") String addressId, String id) {
        Address address = addressRepository.findById(addressId).orElseThrow(() -> {
            log.warn("Address not found: addressId={}, userId={}", addressId, id);
            return new BusinessException("Address Not Found", "NOT_FOUND", HttpStatus.NOT_FOUND);
        });

        if (!address.getUser().getId().equals(id)) {
            log.warn("Unauthorized address usage: userId={}, addressId={}, ownerId={}",
                    id, addressId, address.getUser().getId());
            throw new BusinessException("This Address Not Yours", "ADDRESS_NOT_YOURS", HttpStatus.BAD_REQUEST);
        }
        return address;
    }

    private Cart getValidCart(String id) {
        Cart cart = cartRepository.findByUserId(id).orElseThrow(() -> {
            log.warn("Cart not found: userId={}", id);
            return new BusinessException("Cart Not Found", "NOT_FOUND", HttpStatus.NOT_FOUND);
        });
        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            log.warn("Cart is empty: userId={}", id);
            throw new BusinessException("Cart is empty", "CART_EMPTY", HttpStatus.NOT_FOUND);
        }
        return cart;
    }


}
