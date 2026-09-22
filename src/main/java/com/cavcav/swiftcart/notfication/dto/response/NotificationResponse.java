package com.cavcav.swiftcart.notfication.dto.response;


import com.cavcav.swiftcart.notfication.model.Notification;
import com.cavcav.swiftcart.notfication.model.NotificationType;

import java.time.LocalDateTime;

public record NotificationResponse(
        String id,
        NotificationType type,
        String title,
        String message,
        Boolean isRead,
        String referenceId,
        LocalDateTime createdAt
) {
    public static NotificationResponse from(Notification notification) {
        return new NotificationResponse(
                notification.getId(),
                notification.getType(),
                notification.getTitle(),
                notification.getMessage(),
                notification.getIsRead(),
                notification.getReferenceId(),
                notification.getCreatedAt()
        );
    }
}