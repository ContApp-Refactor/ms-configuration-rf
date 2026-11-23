package co.unicauca.edu.co.contables.auxiliary.domain.ports.notification;

import co.unicauca.edu.co.contables.auxiliary.domain.models.notification.Notification;

import java.util.UUID;

/**
 * @brief Puerto de escritura para notificaciones.
 */
public interface INotificationCommandRepositoryPort {
    Notification save(Notification notification);
    void markAsRead(UUID notificationId, String userId);
    void markAllAsRead(String userId);
    void deleteByIdAndUserId(UUID notificationId, String userId);
    void deleteAllReadByUserId(String userId);
}
