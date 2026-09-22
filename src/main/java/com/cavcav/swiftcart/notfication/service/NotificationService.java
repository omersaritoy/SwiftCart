package com.cavcav.swiftcart.notfication.service;


import com.cavcav.swiftcart.common.exception.BusinessException;
import com.cavcav.swiftcart.common.response.PaginationResponse;
import com.cavcav.swiftcart.notfication.dto.response.NotificationResponse;
import com.cavcav.swiftcart.notfication.repository.NotificationRepository;
import com.cavcav.swiftcart.user.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public PaginationResponse<NotificationResponse> getMyNotifications(int page, int size, User user) {
        log.info("Fetching notifications: userId={}", user.getId());

        var notifications = notificationRepository
                .findByUserIdOrderByCreatedAtDesc(user.getId(), PageRequest.of(page, size));

        return PaginationResponse.of(notifications.map(NotificationResponse::from));
    }

    public long getUnreadCount(User user) {
        return notificationRepository.countByUserIdAndIsReadFalse(user.getId());
    }

    @Transactional
    public void markAsRead(String notificationId, User user) {
        log.info("Marking notification as read: id={}, userId={}", notificationId, user.getId());
        notificationRepository.markAsRead(notificationId, user.getId());
    }

    @Transactional
    public void markAllAsRead(User user) {
        log.info("Marking all notifications as read: userId={}", user.getId());
        notificationRepository.markAllAsRead(user.getId());
    }

    @Transactional
    public void deleteNotification(String notificationId, User user) {
        log.info("Deleting notification: id={}, userId={}", notificationId, user.getId());

        var notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new BusinessException(
                        "Notification not found",
                        "NOTIFICATION_NOT_FOUND",
                        HttpStatus.NOT_FOUND
                ));

        if (!notification.getUser().getId().equals(user.getId())) {
            throw new BusinessException(
                    "You are not authorized to delete this notification",
                    "NOTIFICATION_ACCESS_DENIED",
                    HttpStatus.FORBIDDEN
            );
        }

        notificationRepository.delete(notification);
        log.info("Notification deleted: id={}", notificationId);
    }
}
