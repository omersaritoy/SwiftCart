package com.cavcav.swiftcart.notfication.consumer;

import com.cavcav.swiftcart.common.config.RabbitMQConfig;
import com.cavcav.swiftcart.notfication.event.OrderCancelledEvent;
import com.cavcav.swiftcart.notfication.event.OrderCreatedEvent;
import com.cavcav.swiftcart.notfication.model.Notification;
import com.cavcav.swiftcart.notfication.model.NotificationType;
import com.cavcav.swiftcart.notfication.repository.NotificationRepository;
import com.cavcav.swiftcart.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationConsumer {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    @RabbitListener(queues = RabbitMQConfig.NOTIFICATION_QUEUE)
    public void handleOrderCreated(OrderCreatedEvent event) {
        log.info("Notification received - OrderCreated: orderId={}", event.orderId());

        userRepository.findById(event.userId()).ifPresent(user -> {
            Notification notification = Notification.builder()
                    .user(user)
                    .type(NotificationType.ORDER_CREATED)
                    .title("Order Confirmed!")
                    .message("Your order #" + event.orderId() + " has been confirmed. Total: $" + event.totalPrice())
                    .isRead(false)
                    .referenceId(event.orderId())
                    .build();

            notificationRepository.save(notification);
            log.info("Notification saved: orderId={}, userId={}", event.orderId(), event.userId());
        });
    }
    @RabbitListener(queues = RabbitMQConfig.NOTIFICATION_QUEUE)
    public void handleOrderCancelled(OrderCancelledEvent event) {
        log.info("Notification received - OrderCancelled: orderId={}", event.orderId());

        userRepository.findById(event.userId()).ifPresent(user -> {
            Notification notification = Notification.builder()
                    .user(user)
                    .type(NotificationType.ORDER_CANCELLED)
                    .title("Order Cancelled")
                    .message("Your order #" + event.orderId() + " has been cancelled.")
                    .isRead(false)
                    .referenceId(event.orderId())
                    .build();

            notificationRepository.save(notification);
        });
    }
}
