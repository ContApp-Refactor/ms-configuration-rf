package co.unicauca.edu.co.contables.auxiliary.infrastructure.in.rest.controller.notification;

import co.unicauca.edu.co.contables.auxiliary.application.ports.in.notification.INotificationCommandPort;
import co.unicauca.edu.co.contables.auxiliary.application.ports.in.notification.INotificationQueryPort;
import co.unicauca.edu.co.contables.auxiliary.infrastructure.in.rest.dto.response.ResponseDTO;
import co.unicauca.edu.co.contables.auxiliary.infrastructure.in.rest.dto.response.notification.NotificationResponse;
import co.unicauca.edu.co.contables.auxiliary.infrastructure.in.rest.mapper.INotificationRestMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notifications")
public class NotificationController {

    private final INotificationQueryPort notificationQueryPort;
    private final INotificationCommandPort notificationCommandPort;
    private final INotificationRestMapper notificationRestMapper;

    @GetMapping
    public ResponseEntity<ResponseDTO<List<NotificationResponse>>> list(
            @RequestParam String userId,
            @RequestParam(required = false) Boolean unreadOnly,
            @RequestParam(required = false) Integer limit
    ) {
        List<NotificationResponse> data = notificationQueryPort.list(userId, unreadOnly, limit).stream()
                .map(notificationRestMapper::toResponse)
                .toList();
        return ResponseDTO.<List<NotificationResponse>>builder()
                .data(data).statusCode(200).message("Notifications retrieved successfully.")
                .build().of();
    }

    @GetMapping("/unread-count")
    public ResponseEntity<ResponseDTO<Long>> unreadCount(@RequestParam String userId) {
        long count = notificationQueryPort.countUnread(userId);
        return ResponseDTO.<Long>builder()
                .data(count).statusCode(200).message("Unread count retrieved successfully.")
                .build().of();
    }

    @PatchMapping("/{notificationId}/read")
    public ResponseEntity<ResponseDTO<Void>> markAsRead(
            @PathVariable UUID notificationId,
            @RequestParam String userId
    ) {
        notificationCommandPort.markAsRead(notificationId, userId);
        return ResponseDTO.<Void>builder().statusCode(200).message("Notification marked as read.").build().of();
    }

    @PatchMapping("/read-all")
    public ResponseEntity<ResponseDTO<Void>> markAllAsRead(@RequestParam String userId) {
        notificationCommandPort.markAllAsRead(userId);
        return ResponseDTO.<Void>builder().statusCode(200).message("All notifications marked as read.").build().of();
    }

    @DeleteMapping("/{notificationId}")
    public ResponseEntity<ResponseDTO<Void>> delete(
            @PathVariable UUID notificationId,
            @RequestParam String userId
    ) {
        notificationCommandPort.delete(notificationId, userId);
        return ResponseDTO.<Void>builder().statusCode(200).message("Notification deleted.").build().of();
    }

    @DeleteMapping("/read-all")
    public ResponseEntity<ResponseDTO<Void>> deleteAllRead(@RequestParam String userId) {
        notificationCommandPort.deleteAllRead(userId);
        return ResponseDTO.<Void>builder().statusCode(200).message("All read notifications deleted.").build().of();
    }
}
