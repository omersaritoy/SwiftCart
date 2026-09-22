package com.cavcav.swiftcart.notfication.contorller;


import com.cavcav.swiftcart.auth.security.UserPrincipal;
import com.cavcav.swiftcart.common.response.ApiResponse;
import com.cavcav.swiftcart.common.response.PaginationResponse;
import com.cavcav.swiftcart.notfication.dto.response.NotificationResponse;
import com.cavcav.swiftcart.notfication.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public ResponseEntity<ApiResponse<PaginationResponse<NotificationResponse>>> getMyNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success(
                notificationService.getMyNotifications(page, size, principal.user())));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<ApiResponse<Long>> getUnreadCount(
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success(
                notificationService.getUnreadCount(principal.user())));
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<ApiResponse<?>> markAsRead(
            @PathVariable String id,
            @AuthenticationPrincipal UserPrincipal principal) {
        notificationService.markAsRead(id, principal.user());
        return ResponseEntity.ok(ApiResponse.success("Notification marked as read", null));
    }

    @PatchMapping("/read-all")
    public ResponseEntity<ApiResponse<?>> markAllAsRead(
            @AuthenticationPrincipal UserPrincipal principal) {
        notificationService.markAllAsRead(principal.user());
        return ResponseEntity.ok(ApiResponse.success("All notifications marked as read", null));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> deleteNotification(
            @PathVariable String id,
            @AuthenticationPrincipal UserPrincipal principal) {
        notificationService.deleteNotification(id, principal.user());
        return ResponseEntity.ok(ApiResponse.success("Notification deleted successfully", null));
    }
}
