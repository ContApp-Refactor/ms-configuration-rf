package co.unicauca.edu.co.contables.auxiliary.application.ports.in.notification;

import co.unicauca.edu.co.contables.auxiliary.domain.models.notification.Notification;

import java.util.List;

/**
 * @brief Puerto de entrada de consulta de notificaciones.
 */
public interface INotificationQueryPort {
    List<Notification> list(String userId, Boolean unreadOnly, Integer limit);
    long countUnread(String userId);
}
