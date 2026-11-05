package co.unicauca.edu.co.contables.auxiliary.infrastructure.out.persistence.mapper.notification;

import co.unicauca.edu.co.contables.auxiliary.domain.models.notification.Notification;
import co.unicauca.edu.co.contables.auxiliary.infrastructure.out.persistence.entity.notification.NotificationEntity;
import org.springframework.stereotype.Component;

/**
 * @brief Mapper manual entre {@link Notification} (dominio) y {@link NotificationEntity} (JPA).
 */
@Component
public class INotificationPersistenceMapper {

    public NotificationEntity toEntity(Notification notification) {
        if (notification == null) return null;
        NotificationEntity entity = new NotificationEntity();
        entity.setNotificationId(notification.getNotificationId());
        entity.setUserId(notification.getUserId());
        entity.setType(notification.getType());
        entity.setTitle(notification.getTitle());
        entity.setMessage(notification.getMessage());
        entity.setReferenceId(notification.getReferenceId());
        entity.setReferencePublicId(notification.getReferencePublicId());
        entity.setRead(notification.isRead());
        entity.setCreatedAt(notification.getCreatedAt());
        entity.setReadAt(notification.getReadAt());
        return entity;
    }

    public Notification toDomain(NotificationEntity entity) {
        if (entity == null) return null;
        return Notification.builder()
                .notificationId(entity.getNotificationId())
                .userId(entity.getUserId())
                .type(entity.getType())
                .title(entity.getTitle())
                .message(entity.getMessage())
                .referenceId(entity.getReferenceId())
                .referencePublicId(entity.getReferencePublicId())
                .read(entity.isRead())
                .createdAt(entity.getCreatedAt())
                .readAt(entity.getReadAt())
                .build();
    }
}
