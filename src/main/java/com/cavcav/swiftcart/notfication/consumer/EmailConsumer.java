package com.cavcav.swiftcart.notfication.consumer;


import com.cavcav.swiftcart.common.config.RabbitMQConfig;
import com.cavcav.swiftcart.notfication.event.OrderCancelledEvent;
import com.cavcav.swiftcart.notfication.event.OrderCreatedEvent;
import com.cavcav.swiftcart.notfication.event.PaymentFailedEvent;
import com.cavcav.swiftcart.notfication.event.PaymentSuccessEvent;
import com.cavcav.swiftcart.notfication.service.EmailService;
import com.cavcav.swiftcart.order.model.Order;
import com.cavcav.swiftcart.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
@Component
@RequiredArgsConstructor
@Slf4j
public class EmailConsumer {

    private final EmailService emailService;
    private final OrderRepository orderRepository;

    @RabbitListener(queues = RabbitMQConfig.EMAIL_QUEUE)
    public void handlePaymentSuccess(PaymentSuccessEvent event) {
        log.info("Email received - PaymentSuccess: orderId={}", event.orderId());
        orderRepository.findById(event.orderId()).ifPresent(order ->
                emailService.sendPaymentSuccessEmail(event.userEmail(), order));
    }

    @RabbitListener(queues = RabbitMQConfig.EMAIL_QUEUE)
    public void handlePaymentFailed(PaymentFailedEvent event) {
        log.info("Email received - PaymentFailed: orderId={}", event.orderId());
        orderRepository.findById(event.orderId()).ifPresent(order ->
                emailService.sendPaymentFailedEmail(event.userEmail(), order));
    }

    @RabbitListener(queues = RabbitMQConfig.EMAIL_QUEUE)
    public void handleOrderCreated(OrderCreatedEvent event) {
        log.info("Email received - OrderCreated: orderId={}", event.orderId());
        orderRepository.findById(event.orderId()).ifPresent(order ->
                emailService.sendOrderConfirmationEmail(event.userEmail(), order));
    }

    @RabbitListener(queues = RabbitMQConfig.EMAIL_QUEUE)
    public void handleOrderCancelled(OrderCancelledEvent event) {
        log.info("Email received - OrderCancelled: orderId={}", event.orderId());
        orderRepository.findById(event.orderId()).ifPresent(order ->
                emailService.sendOrderCancellationEmail(event.userEmail(), order));
    }
}