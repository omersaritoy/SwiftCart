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
import com.cavcav.swiftcart.order.repository.OrderItemRepository;
import com.cavcav.swiftcart.order.repository.OrderRepository;
import com.cavcav.swiftcart.product.model.Product;
import com.cavcav.swiftcart.product.repository.ProductRepository;
import com.cavcav.swiftcart.user.model.Address;
import com.cavcav.swiftcart.user.model.User;
import com.cavcav.swiftcart.user.repository.AddressRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CartRepository cartRepository;
    private final AddressRepository addressRepository;
    private final ProductRepository productRepository;
    private final EmailService emailService;
    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request, User user) {
        log.info("Creating order: userId={}, addressId={}", user.getId(), request.addressId());

        Cart cart = getValidCart(user);
        Address address = getValidAddress(request.addressId(), user);
        validateStock(cart);

        Order order = buildOrder(user, address);
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

    private Cart getValidCart(User user) {
        Cart cart = cartRepository.findByUserId(user.getId())
                .orElseThrow(() -> {
                    log.warn("Cart not found: userId={}", user.getId());
                    return new BusinessException("Cart Not Found", "NOT_FOUND", HttpStatus.NOT_FOUND);
                });
        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            log.warn("Cart is empty: userId={}", user.getId());
            throw new BusinessException("Cart is empty", "CART_EMPTY", HttpStatus.NOT_FOUND);
        }
        return cart;
    }
    private Address getValidAddress(String addressId, User user) {
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> {
                    log.warn("Address not found: addressId={}, userId={}", addressId, user.getId());
                    return new BusinessException("Address Not Found", "NOT_FOUND", HttpStatus.NOT_FOUND);
                });
        if (!address.getUser().getId().equals(user.getId())) {
            log.warn("Unauthorized address usage: userId={}, addressId={}, ownerId={}",
                    user.getId(), addressId, address.getUser().getId());
            throw new BusinessException("This Address Not Yours", "ADDRESS_NOT_YOURS", HttpStatus.BAD_REQUEST);
        }
        return address;
    }


    private Order buildOrder(User user, Address address) {
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
                    .priceAtOrder(product.getPrice())
                    .quantity(quantity)
                    .totalPrice(itemTotal)
                    .build());

            orderTotal = orderTotal.add(itemTotal);
        }
        return orderTotal;
    }

    public PaginationResponse<OrderSummaryResponse> getMyOrders(int page, int size, String sortBy, String direction, User user) {
        log.info("Get my orders request: userId={}, page={}, size={}, sortBy={}, direction={}", user.getId(), page, size, sortBy, direction);

        Sort sort = direction.equalsIgnoreCase("DESC")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        log.info("Fetching orders: userId={}, page={}, size={}, sort={}", user.getId(), page, size, sort);

        Page<Order> orders = orderRepository.findAll(PageRequest.of(page, size, sort));

        log.info("Orders fetched successfully: userId={}, totalElements={}, totalPages={}", user.getId(), orders.getTotalElements(), orders.getTotalPages());

        return PaginationResponse.of(orders.map(OrderSummaryResponse::from));
    }


}
