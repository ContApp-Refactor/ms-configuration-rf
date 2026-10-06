package co.unicauca.edu.co.contables.auxiliary.application.useCase.notification;

import co.unicauca.edu.co.contables.auxiliary.application.ports.in.notification.INotificationQueryPort;
import co.unicauca.edu.co.contables.auxiliary.domain.models.notification.Notification;
import co.unicauca.edu.co.contables.auxiliary.domain.ports.notification.INotificationQueryRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationQueryUC implements INotificationQueryPort {

    private final INotificationQueryRepositoryPort notificationQueryRepositoryPort;

    @Override
    public List<Notification> list(String userId, Boolean unreadOnly, Integer limit) {
        return notificationQueryRepositoryPort.findByUserId(userId, unreadOnly, limit);
    }

    @Override
    public long countUnread(String userId) {
        return notificationQueryRepositoryPort.countUnread(userId);
    }
}
