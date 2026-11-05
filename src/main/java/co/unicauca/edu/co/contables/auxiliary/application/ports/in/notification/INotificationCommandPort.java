package co.unicauca.edu.co.contables.auxiliary.application.ports.in.notification;

import java.util.UUID;

/**
 * @brief Puerto de entrada de comandos sobre notificaciones.
 */
public interface INotificationCommandPort {
    void markAsRead(UUID notificationId, String userId);
    void markAllAsRead(String userId);
    void delete(UUID notificationId, String userId);
    void deleteAllRead(String userId);
}
