package com.cavcav.swiftcart.notfication.listener;


import com.cavcav.swiftcart.notfication.dto.OrderCancelledEvent;
import com.cavcav.swiftcart.notfication.dto.OrderCreatedEvent;
import com.cavcav.swiftcart.notfication.service.EmailService;
import com.cavcav.swiftcart.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventListener {

    private final EmailService emailService;
    private final OrderRepository orderRepository;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onOrderCreated(OrderCreatedEvent event) {
        orderRepository.findById(event.orderId())
                .ifPresentOrElse(
                        order -> emailService.sendOrderConfirmationEmail(event.userEmail(), order),
                        () -> log.error("Order not found for confirmation email: orderId={}", event.orderId())
                );
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onOrderCancelled(OrderCancelledEvent event) {
        orderRepository.findById(event.orderId())
                .ifPresentOrElse(
                        order -> emailService.sendOrderCancellationEmail(event.userEmail(), order),
                        () -> log.error("Order not found for cancellation email: orderId={}", event.orderId())
                );
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onOrderStatusChanged(OrderCancelledEvent.OrderStatusChangedEvent event) {
        orderRepository.findById(event.orderId())
                .ifPresentOrElse(
                        order -> emailService.sendOrderStatusChangedEmail(event.userEmail(), order, event.newStatus()),
                        () -> log.error("Order not found for status change email: orderId={}", event.orderId())
                );
    }
}
