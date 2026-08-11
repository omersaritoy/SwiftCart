package com.cavcav.swiftcart.order.controller;


import com.cavcav.swiftcart.common.response.ApiResponse;
import com.cavcav.swiftcart.common.response.PaginationResponse;
import com.cavcav.swiftcart.order.dto.request.CreateOrderRequest;
import com.cavcav.swiftcart.order.dto.request.UpdateOrderStatusRequest;
import com.cavcav.swiftcart.order.dto.response.OrderResponse;
import com.cavcav.swiftcart.order.dto.response.OrderSummaryResponse;
import com.cavcav.swiftcart.order.service.OrderService;
import com.cavcav.swiftcart.user.model.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(
            @Valid @RequestBody CreateOrderRequest request,
            @AuthenticationPrincipal User user
    ) {
        OrderResponse response = orderService.createOrder(request, user);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/me")
    public ResponseEntity<PaginationResponse<OrderSummaryResponse>> getMyOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String direction,
            @AuthenticationPrincipal User user
    ) {
        PaginationResponse<OrderSummaryResponse> response =
                orderService.getMyOrders(page, size, sortBy, direction, user);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getOrderById(
            @PathVariable String orderId,
            @AuthenticationPrincipal User user
    ) {
        OrderResponse response = orderService.getOrderById(orderId, user);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<Void> cancelOrder(
            @PathVariable String orderId,
            @AuthenticationPrincipal User user
    ) {
        orderService.cancelOrder(orderId, user);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/seller")
    public ResponseEntity<PaginationResponse<OrderSummaryResponse>> getSellerOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String direction,
            @AuthenticationPrincipal User seller
    ) {
        PaginationResponse<OrderSummaryResponse> response =
                orderService.getSellerOrders(page, size, sortBy, direction, seller);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{orderId}/status")
    public ResponseEntity<OrderResponse> updateOrderStatus(
            @PathVariable String orderId,
            @Valid @RequestBody UpdateOrderStatusRequest request,
            @AuthenticationPrincipal User seller
    ) {
        OrderResponse response = orderService.updateOrderStatus(orderId, request, seller);
        return ResponseEntity.ok(response);
    }
}