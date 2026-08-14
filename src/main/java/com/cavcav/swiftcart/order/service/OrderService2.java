package com.cavcav.swiftcart.order.service;

import com.cavcav.swiftcart.cart.Repository.CartRepository;
import com.cavcav.swiftcart.cart.model.Cart;
import com.cavcav.swiftcart.cart.model.CartItem;
import com.cavcav.swiftcart.common.exception.BusinessException;
import com.cavcav.swiftcart.common.response.PaginationResponse;
import com.cavcav.swiftcart.notfication.dto.OrderCancelledEvent;
import com.cavcav.swiftcart.notfication.service.EmailService;
import com.cavcav.swiftcart.order.dto.request.CreateOrderRequest;
import com.cavcav.swiftcart.order.dto.request.UpdateOrderStatusRequest;
import com.cavcav.swiftcart.order.dto.response.OrderResponse;
import com.cavcav.swiftcart.order.dto.response.OrderSummaryResponse;
import com.cavcav.swiftcart.order.model.Order;
import com.cavcav.swiftcart.order.model.OrderItem;
import com.cavcav.swiftcart.order.model.OrderStatus;
import com.cavcav.swiftcart.order.repository.OrderRepository;
import com.cavcav.swiftcart.product.model.Product;
import com.cavcav.swiftcart.product.repository.ProductRepository;
import com.cavcav.swiftcart.user.model.Address;
import com.cavcav.swiftcart.user.model.Role;
import com.cavcav.swiftcart.user.model.User;
import com.cavcav.swiftcart.user.repository.AddressRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService2 {
    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final AddressRepository addressRepository;
    private final ProductRepository productRepository;
    private final EmailService emailService;
    private static final Set<String> ALLOWED_ORDER_SORT_FIELDS = Set.of("createdAt", "totalPrice", "status");
    private static final Map<OrderStatus, Set<OrderStatus>> ALLOWED_TRANSITIONS = Map.of(
            OrderStatus.PENDING, Set.of(OrderStatus.PAID),
            OrderStatus.PAID, Set.of(OrderStatus.PROCESSING),
            OrderStatus.PROCESSING, Set.of(OrderStatus.SHIPPED),
            OrderStatus.SHIPPED, Set.of(OrderStatus.DELIVERED),
            OrderStatus.DELIVERED, Set.of(),
            OrderStatus.CANCELLED, Set.of()
    );
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request, User user) {
        log.info("Creating order: userId={}, addressId={}", user.getId(), request.addressId());

        Cart cart = getValidCart(user);
        Address address = getValidAddress(request.addressId(), user);

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


    @Transactional(readOnly = true)
    public PaginationResponse<OrderSummaryResponse> getMyOrders(int page, int size, String sortBy, String direction, User user) {
        log.info("Get my orders request: userId={}, page={}, size={}, sortBy={}, direction={}",
                user.getId(), page, size, sortBy, direction);

        Sort sort = resolveSort(sortBy, direction);

        Page<Order> orders = orderRepository.findByUserId(user.getId(), PageRequest.of(page, size, sort));
        log.info("Orders fetched successfully: userId={}, totalElements={}, totalPages={}",
                user.getId(), orders.getTotalElements(), orders.getTotalPages());

        return PaginationResponse.of(orders.map(OrderSummaryResponse::from));
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrderById(String orderId, User user) {
        log.info("Fetching order: orderId={}, userId={}", orderId, user.getId());

        Order order = getOrder(orderId);

        if (!order.getUser().getId().equals(user.getId())) {
            log.warn("Unauthorized order access: orderId={}, userId={}, ownerId={}",
                    orderId, user.getId(), order.getUser().getId());
            throw new BusinessException("You are not authorized to view this order", "ORDER_ACCESS_DENIED", HttpStatus.FORBIDDEN);
        }

        log.info("Order fetched: orderId={}, userId={}", orderId, user.getId());
        return OrderResponse.from(order);
    }

    @Transactional
    public void cancelOrder(String orderId, User user) {
        Order order = getOrder(orderId);
        if (!order.getUser().getId().equals(user.getId())) {
            log.warn("Unauthorized order access: orderId={}, userId={}, ownerId={}", orderId, user.getId(), order.getUser().getId());
            throw new BusinessException("You are not authorized to view this order", "ORDER_ACCESS_DENIED", HttpStatus.FORBIDDEN);
        }
        if (order.getStatus() != OrderStatus.PENDING && order.getStatus() != OrderStatus.PAID) {
            log.warn("Order cannot be cancelled: orderId={}, status={}", orderId, order.getStatus());

            throw new BusinessException("Order cannot be cancelled in its current status", "ORDER_CANNOT_BE_CANCELLED", HttpStatus.BAD_REQUEST);
        }
        order.setStatus(OrderStatus.CANCELLED);
        order.setCancelledAt(LocalDateTime.now());
        restoreStock(order);

        log.info("Order cancelled: orderId={}, userId={}", orderId, user.getId());

        emailService.sendOrderCancellationEmail(user.getEmail(), order);
    }

    @Transactional(readOnly = true)
    public PaginationResponse<OrderSummaryResponse> getSellerOrders(int page, int size, String sortBy, String direction, User seller) {
        if (seller.getRole() != Role.SELLER) {
            log.warn("Unauthorized seller orders access attempt: userId={}, role={}", seller.getId(), seller.getRole());
            throw new BusinessException("User Not A Seller", "USER_NOT_SELLER", HttpStatus.FORBIDDEN);
        }

        Sort sort = resolveSort(sortBy, direction);

        log.info("Fetching seller orders: sellerId={}, page={}, size={}, sort={}", seller.getId(), page, size, sort);

        Page<Order> orders = orderRepository.findBySellerId(seller.getId(), PageRequest.of(page, size, sort));
        log.info("Seller orders fetched: sellerId={}, page={}, size={}, totalElements={}",
                seller.getId(), page, size, orders.getTotalElements());

        return PaginationResponse.of(orders.map(OrderSummaryResponse::from));
    }




    @Transactional
    public OrderResponse updateOrderStatus(String orderId, UpdateOrderStatusRequest request, User seller) {
        OrderStatus newStatus = request.status();
        log.info("Update order status request: orderId={}, newStatus={}, userId={}", orderId, newStatus, seller.getId());

        Order order = getOrder(orderId);

        if (seller.getRole() != Role.ADMIN) {
            boolean sellsInThisOrder = order.getItems().stream()
                    .anyMatch(item -> item.getProduct().getSeller().getId().equals(seller.getId()));

            if (!sellsInThisOrder) {
                log.warn("Unauthorized order status update attempt: orderId={}, sellerId={}", orderId, seller.getId());
                throw new BusinessException("You do not sell in this order", "ORDER_ACCESS_DENIED", HttpStatus.FORBIDDEN);
            }
        }

        OrderStatus currentStatus = order.getStatus();
        Set<OrderStatus> allowedNextStatuses = ALLOWED_TRANSITIONS.getOrDefault(currentStatus, Set.of());

        if (!allowedNextStatuses.contains(newStatus)) {
            log.warn("Invalid status transition: orderId={}, from={}, to={}", orderId, currentStatus, newStatus);
            throw new BusinessException(
                    "Cannot transition order from " + currentStatus + " to " + newStatus,
                    "INVALID_STATUS_TRANSITION",
                    HttpStatus.BAD_REQUEST
            );
        }

        order.setStatus(newStatus);
        log.info("Order status updated: orderId={}, from={}, to={}", orderId, currentStatus, newStatus);

        eventPublisher.publishEvent(new OrderCancelledEvent.OrderStatusChangedEvent(order.getId(), order.getUser().getEmail(), newStatus));

        return OrderResponse.from(order);
    }
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onOrderStatusChanged(OrderCancelledEvent.OrderStatusChangedEvent event) {
        orderRepository.findById(event.orderId())
                .ifPresentOrElse(
                        order -> emailService.sendOrderStatusChangedEmail(event.userEmail(), order, event.newStatus()),
                        () -> log.error("Order not found for status change email: orderId={}", event.orderId())
                );
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






    private void restoreStock(Order order) {
        for (OrderItem item : order.getItems()) {

            productRepository.increaseStock(item.getProduct().getId(), item.getQuantity());
        }
    }


    @NotNull
    private Order getOrder(String orderId) {
        return orderRepository.findById(orderId).orElseThrow(() -> {
            log.warn("Order not found: orderId={}", orderId);
            return new BusinessException("Order Not Found", "ORDER_NOT_FOUND", HttpStatus.NOT_FOUND);
        });
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
                    .priceAtOrder(item.getPriceAtAddedTime())
                    .quantity(quantity)
                    .totalPrice(itemTotal)
                    .build());

            orderTotal = orderTotal.add(itemTotal);
        }
        return orderTotal;
    }


}
