package co.unicauca.edu.co.contables.auxiliary.infrastructure.in.sse;

import co.unicauca.edu.co.contables.auxiliary.application.ports.out.INotificationPusherPort;
import co.unicauca.edu.co.contables.auxiliary.domain.models.notification.Notification;
import co.unicauca.edu.co.contables.auxiliary.infrastructure.in.rest.dto.response.notification.NotificationResponse;
import co.unicauca.edu.co.contables.auxiliary.infrastructure.in.rest.mapper.INotificationRestMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * @brief Adaptador SSE para empujar notificaciones en tiempo real.
 */
@Component
@RequiredArgsConstructor
public class SseNotificationPusherAdapter implements INotificationPusherPort {

    private final SseEmitterRegistry sseEmitterRegistry;
    private final INotificationRestMapper notificationRestMapper;

    @Override
    public void push(String userId, Notification notification) {
        if (userId == null || notification == null) {
            return;
        }
        NotificationResponse payload = notificationRestMapper.toResponse(notification);
        sseEmitterRegistry.sendToUser(userId, "notification", payload);
    }
}
