package co.unicauca.edu.co.contables.auxiliary.infrastructure.in.rest.mapper;

import co.unicauca.edu.co.contables.auxiliary.domain.models.notification.Notification;
import co.unicauca.edu.co.contables.auxiliary.infrastructure.in.rest.dto.response.notification.NotificationResponse;
import org.springframework.stereotype.Component;

@Component
public class INotificationRestMapper {

    public NotificationResponse toResponse(Notification n) {
        if (n == null) return null;
        return NotificationResponse.builder()
                .notificationId(n.getNotificationId())
                .userId(n.getUserId())
                .type(n.getType())
                .title(n.getTitle())
                .message(n.getMessage())
                .referenceId(n.getReferenceId())
                .referencePublicId(n.getReferencePublicId())
                .read(n.isRead())
                .createdAt(n.getCreatedAt())
                .readAt(n.getReadAt())
                .build();
    }
}
