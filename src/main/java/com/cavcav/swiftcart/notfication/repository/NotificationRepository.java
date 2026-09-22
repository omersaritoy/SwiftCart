package com.cavcav.swiftcart.notfication.repository;

import com.cavcav.swiftcart.notfication.model.Notification;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface NotificationRepository extends JpaRepository<Notification, String> {
    Page<Notification> findByUserIdOrderByCreatedAtDesc(String userId, Pageable pageable);
    long countByUserIdAndIsReadFalse(String userId);

    @Modifying
    @Query("update Notification n set n.isRead=TRUE  where n.id=:id and n.user.id=:userId")
    void markAsRead(@Param("id") String id, @Param("userId") String userId);

    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.user.id = :userId")
    void markAllAsRead(@Param("userId") String userId);
}

