package co.unicauca.edu.co.contables.auxiliary.application.useCase.notification;

import co.unicauca.edu.co.contables.auxiliary.application.ports.in.notification.INotificationCommandPort;
import co.unicauca.edu.co.contables.auxiliary.domain.ports.notification.INotificationCommandRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotificationCommandUC implements INotificationCommandPort {

    private final INotificationCommandRepositoryPort notificationCommandRepositoryPort;

    @Override
    public void markAsRead(UUID notificationId, String userId) {
        notificationCommandRepositoryPort.markAsRead(notificationId, userId);
    }

    @Override
    public void markAllAsRead(String userId) {
        notificationCommandRepositoryPort.markAllAsRead(userId);
    }

    @Override
    public void delete(UUID notificationId, String userId) {
        notificationCommandRepositoryPort.deleteByIdAndUserId(notificationId, userId);
    }

    @Override
    public void deleteAllRead(String userId) {
        notificationCommandRepositoryPort.deleteAllReadByUserId(userId);
    }
}
